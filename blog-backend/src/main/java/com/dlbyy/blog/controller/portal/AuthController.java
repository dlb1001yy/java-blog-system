package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.security.JwtTokenProvider;
import com.dlbyy.blog.service.LoginAttemptService;
import com.dlbyy.blog.utils.CookieUtils;
import com.dlbyy.blog.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            return Result.error(400, "用户名不能为空");
        }

        // 1) 账户锁定检查（基于 Redis 的连续失败计数 / 锁定时间）
        if (loginAttemptService.isLocked(username)) {
            long remain = loginAttemptService.getRemainingLockMillis(username);
            long minutes = Math.max(1, remain / 60000);
            return Result.error(423, "账户已被锁定，请 " + minutes + " 分钟后再试");
        }

        // 2) 单 IP 维度登录限流
        String clientIp = getClientIp(httpRequest);
        if (!loginAttemptService.tryAcquireIp(clientIp)) {
            return Result.error(429, "登录尝试过于频繁，请稍后再试");
        }

        // 3) 认证（Spring Security 内部已用 BCrypt 校验密码）
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            // 登录成功：清除失败计数
            loginAttemptService.onLoginSuccess(username);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // refresh token 通过 HTTP-only Cookie 下发，前端不再持有
            cookieUtils.addRefreshCookie(httpResponse, refreshToken);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
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
        // 从 HTTP-only Cookie 读取 refresh token，无需前端传参
        String refreshToken = cookieUtils.getRefreshTokenFromRequest(httpRequest);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("RefreshToken 缺失或已过期");
        }
        if (!jwtUtils.isValidRefreshToken(refreshToken)) {
            cookieUtils.clearRefreshCookie(httpResponse);
            throw new BusinessException("RefreshToken 无效或已吊销，请重新登录");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtUtils.generateAccessToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
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
        // 清除 refresh Cookie
        cookieUtils.clearRefreshCookie(httpResponse);
        return Result.success("退出成功", null);
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
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
