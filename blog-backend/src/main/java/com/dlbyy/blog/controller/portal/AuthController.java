package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.security.JwtTokenProvider;
import com.dlbyy.blog.service.CaptchaService;
import com.dlbyy.blog.service.LoginAttemptService;
import com.dlbyy.blog.service.UserService;
import com.dlbyy.blog.utils.CookieUtils;
import com.dlbyy.blog.utils.JwtUtils;
import com.dlbyy.blog.utils.PasswordStrengthValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：登录 / 刷新令牌 / 登出
 * 登录接入 IP 限流 + 用户名维度账户锁定；refresh token 通过 HTTP-only Cookie 安全下发。
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtils jwtUtils;
    private final LoginAttemptService loginAttemptService;
    private final CookieUtils cookieUtils;
    private final CaptchaService captchaService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 公开注册：用户名 + 邮箱 + 密码（需通过强度校验），注册后角色为普通用户
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();

        // 基础参数校验
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (password == null || !password.equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 密码强度校验
        PasswordStrengthValidator.ValidationResult result = PasswordStrengthValidator.validate(password);
        if (!result.isValid()) {
            throw new BusinessException(result.getMessage());
        }

        // 用户名 / 邮箱唯一性校验
        if (userService.lambdaQuery().eq(User::getUsername, username).count() > 0) {
            throw new BusinessException("用户名已被注册");
        }
        if (userService.lambdaQuery().eq(User::getEmail, email).count() > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户：BCrypt 加密密码，普通用户角色，正常启用状态
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("user");
        user.setStatus(1);
        user.setFailCount(0);
        userService.save(user);
        return Result.success("注册成功", null);
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    public Result<?> getCaptcha() {
        return Result.success(captchaService.generate());
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            return Result.error(400, "用户名不能为空");
        }

        // 支持邮箱登录：账号含 @ 时按邮箱查用户，映射为其用户名后再走原认证流程
        if (username.contains("@")) {
            User user = userService.lambdaQuery().eq(User::getEmail, username).one();
            if (user == null) {
                return Result.error(401, "用户名或密码错误");
            }
            username = user.getUsername();
        }

        // 1) 账户锁定检查（基于 Redis 的连续失败计数 / 锁定时间）
        if (loginAttemptService.isLocked(username)) {
            long remain = loginAttemptService.getRemainingLockMillis(username);
            long minutes = Math.max(1, remain / 60000);
            return Result.error(423, "账户已被锁定，请 " + minutes + " 分钟后再试");
        }

        // 2.5) 图形验证码校验（在限流前拦截自动化攻击，且失败不消耗限流额度）
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.error(400, "验证码错误或已过期");
        }

        // 3) 单 IP 维度登录限流
        String clientIp = getClientIp(httpRequest);
        if (!loginAttemptService.tryAcquireIp(clientIp)) {
            return Result.error(429, "登录尝试过于频繁，请稍后再试");
        }

        // 4) 认证（Spring Security 内部已用 BCrypt 校验密码）
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            // 登录成功：清除失败计数
            loginAttemptService.onLoginSuccess(username);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // refresh token 通过 HTTP-only Cookie 下发，前端不再持有
            // 后台管理端（X-Client-Type: admin）写入独立 Cookie 名，避免与前台账号串号
            boolean isAdmin = isAdminClient(httpRequest);
            cookieUtils.addRefreshCookie(httpResponse, refreshToken, isAdmin);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("username", username);
            return Result.success("登录成功", response);
        } catch (BadCredentialsException e) {
            // 登录失败：累加失败计数，达到阈值则锁定 + 告警
            long lockMillis = loginAttemptService.onLoginFailure(username);
            if (lockMillis > 0) {
                long minutes = Math.max(1, lockMillis / 60000);
                return Result.error(423, "密码错误次数过多，账户已被锁定 " + minutes + " 分钟");
            }
            return Result.error(401, "用户名或密码错误");
        } catch (Exception e) {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PostMapping("/refresh")
    public Result<?> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        boolean isAdmin = isAdminClient(httpRequest);
        // 后台端优先读独立 Cookie，其次 X-Refresh-Token 请求头（兼容移动端非浏览器客户端）
        String refreshToken;
        if (isAdmin) {
            refreshToken = cookieUtils.getAdminRefreshTokenFromRequest(httpRequest);
            if (refreshToken == null || refreshToken.isBlank()) {
                refreshToken = httpRequest.getHeader("X-Refresh-Token");
            }
            // 平滑迁移：旧版后台写入的是 refresh_token Cookie，若其中是 admin 账号的 token 则放行
            if ((refreshToken == null || refreshToken.isBlank())) {
                refreshToken = cookieUtils.getRefreshTokenFromRequest(httpRequest);
                if (refreshToken != null && !refreshToken.isBlank()
                        && (!jwtUtils.isValidRefreshToken(refreshToken) || !isAdminUsername(refreshToken))) {
                    refreshToken = null;
                }
            }
        } else {
            refreshToken = cookieUtils.getRefreshTokenFromRequest(httpRequest);
            if (refreshToken == null || refreshToken.isBlank()) {
                refreshToken = httpRequest.getHeader("X-Refresh-Token");
            }
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("RefreshToken 缺失或已过期");
        }
        if (!jwtUtils.isValidRefreshToken(refreshToken)) {
            if (isAdmin) {
                cookieUtils.clearRefreshCookie(httpResponse, true);
            } else {
                cookieUtils.clearRefreshCookie(httpResponse);
            }
            throw new BusinessException("RefreshToken 无效或已吊销，请重新登录");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        // Refresh Token 轮换：吊销旧 Token，签发新 Token
        jwtUtils.revokeRefreshToken(username, refreshToken);
        String newAccessToken = jwtUtils.generateAccessToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        // 更新对应端的 Cookie（浏览器客户端）
        cookieUtils.addRefreshCookie(httpResponse, newRefreshToken, isAdmin);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("username", username);
        return Result.success("刷新成功", response);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // 吊销 access token
        String bearerToken = httpRequest.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            jwtUtils.addToBlacklist(token);
            String username = jwtUtils.getUsernameFromToken(token);
            if (username != null) {
                // 吊销该用户所有 refresh token
                jwtUtils.revokeAllRefreshTokens(username);
            }
        }
        // 清除 refresh Cookie（按端清除；登出请求头缺失时两个都清）
        if (isAdminClient(httpRequest)) {
            cookieUtils.clearRefreshCookie(httpResponse, true);
        } else if (httpRequest.getHeader("X-Client-Type") != null) {
            cookieUtils.clearRefreshCookie(httpResponse, false);
        } else {
            cookieUtils.clearRefreshCookie(httpResponse, false);
            cookieUtils.clearRefreshCookie(httpResponse, true);
        }
        return Result.success("退出成功", null);
    }

    /**
     * 判断是否为后台管理端请求（X-Client-Type: admin）
     */
    private boolean isAdminClient(HttpServletRequest request) {
        return "admin".equalsIgnoreCase(request.getHeader("X-Client-Type"));
    }

    /**
     * 判断 refresh token 对应账号是否为 admin 角色（用于旧版 Cookie 平滑迁移）
     */
    private boolean isAdminUsername(String refreshToken) {
        try {
            String username = jwtUtils.getUsernameFromToken(refreshToken);
            User user = userService.lambdaQuery().eq(User::getUsername, username).one();
            return user != null && "admin".equals(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取客户端真实 IP（兼容多级反向代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @lombok.Data
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String confirmPassword;
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String captchaId;
        private String captchaCode;
    }
}
