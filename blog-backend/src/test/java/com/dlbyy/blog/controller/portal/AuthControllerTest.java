package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.security.JwtTokenProvider;
import com.dlbyy.blog.service.CaptchaService;
import com.dlbyy.blog.service.LoginAttemptService;
import com.dlbyy.blog.service.UserService;
import com.dlbyy.blog.utils.CookieUtils;
import com.dlbyy.blog.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuthController} 单元测试。
 * 纯 Mockito 单元测试：直接调用 Controller 方法，不启动 Spring 上下文、不使用 MockMvc；
 * 请求 / 响应使用 spring-test 提供的 Mock 对象模拟。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 认证接口测试")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        // @RequiredArgsConstructor 生成的构造器，手动注入全部 mock 协作者（末尾为 CaptchaService/UserService/PasswordEncoder）
        authController = new AuthController(
                authenticationManager, jwtTokenProvider, jwtUtils, loginAttemptService, cookieUtils, captchaService,
                userService, passwordEncoder);
    }

    /** 构造登录请求体 */
    private AuthController.LoginRequest loginRequest(String username, String password) {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    /** 从 Result 中取出 data（Map 结构） */
    @SuppressWarnings("unchecked")
    private Map<String, Object> dataMap(Result<?> result) {
        return (Map<String, Object>) result.getData();
    }

    @Nested
    @DisplayName("login 登录")
    class Login {

        @Test
        @DisplayName("username 为 null 或空白时返回 400，且不与登录防护服务有任何交互")
        void blankUsername_returns400() {
            // 用户名为 null
            Result<?> nullResult = authController.login(
                    loginRequest(null, "password"), new MockHttpServletRequest(), new MockHttpServletResponse());
            // 用户名为空白字符串
            Result<?> blankResult = authController.login(
                    loginRequest("  ", "password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(nullResult.getCode()).isEqualTo(400);
            assertThat(blankResult.getCode()).isEqualTo(400);
            // 用户名非法时不应触发锁定检查 / IP 限流 / 失败计数
            verifyNoInteractions(loginAttemptService);
        }

        @Test
        @DisplayName("账户已锁定时返回 423，且不执行认证")
        void lockedAccount_returns423_withoutAuthentication() {
            when(loginAttemptService.isLocked("admin")).thenReturn(true);
            when(loginAttemptService.getRemainingLockMillis("admin")).thenReturn(600000L);

            Result<?> result = authController.login(
                    loginRequest("admin", "password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(423);
            assertThat(result.getMessage()).contains("锁定");
            // 锁定状态下直接拒绝，不能进入认证流程
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("账户锁定检查优先于验证码：锁定时返回 423，验证码服务完全未被调用")
        void lockedAccount_returns423_beforeCaptchaVerification() {
            when(loginAttemptService.isLocked("admin")).thenReturn(true);
            when(loginAttemptService.getRemainingLockMillis("admin")).thenReturn(600000L);
            // 不 stub captchaService.verify：若锁定检查晚于验证码，将得到 400 而非 423

            Result<?> result = authController.login(
                    loginRequest("admin", "password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(423);
            // 锁定在验证码之前，验证码校验不应被触发
            verify(captchaService, never()).verify(any(), any());
        }

        @Test
        @DisplayName("验证码错误或已过期时返回 400，且不消耗 IP 限流额度、不计失败次数、不进入认证")
        void captchaFailed_returns400_withoutRateLimitConsumption() {
            when(loginAttemptService.isLocked("admin")).thenReturn(false);
            when(captchaService.verify(any(), any())).thenReturn(false);

            Result<?> result = authController.login(
                    loginRequest("admin", "password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("验证码");
            // 验证码失败在限流之前被拦截：不消耗限流额度、不计入失败次数、不触发认证
            verify(captchaService).verify(any(), any());
            verify(loginAttemptService, never()).tryAcquireIp(any());
            verify(loginAttemptService, never()).onLoginFailure(anyString());
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("IP 限流未通过时返回 429")
        void ipRateLimited_returns429() {
            when(captchaService.verify(any(), any())).thenReturn(true);
            when(loginAttemptService.tryAcquireIp(anyString())).thenReturn(false);

            Result<?> result = authController.login(
                    loginRequest("admin", "password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(429);
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("认证成功返回 200，data 含 accessToken/refreshToken/username，并下发 refresh Cookie")
        void success_returns200_withTokens() {
            when(captchaService.verify(any(), any())).thenReturn(true);
            when(loginAttemptService.tryAcquireIp(anyString())).thenReturn(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken("admin", null, Collections.emptyList()));
            when(jwtTokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("refresh-token");

            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();
            Result<?> result = authController.login(loginRequest("admin", "password"), httpRequest, httpResponse);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getMessage()).isEqualTo("登录成功");
            Map<String, Object> data = dataMap(result);
            assertThat(data)
                    .containsEntry("accessToken", "access-token")
                    .containsEntry("refreshToken", "refresh-token")
                    .containsEntry("username", "admin");

            // 登录成功应清除失败计数，并通过 HttpOnly Cookie 下发 refresh token
            verify(loginAttemptService).onLoginSuccess("admin");
            verify(cookieUtils).addRefreshCookie(httpResponse, "refresh-token");
        }

        @Test
        @DisplayName("凭证错误且未达到锁定阈值时返回 401")
        void badCredentials_belowThreshold_returns401() {
            when(captchaService.verify(any(), any())).thenReturn(true);
            when(loginAttemptService.tryAcquireIp(anyString())).thenReturn(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("用户名或密码错误"));
            when(loginAttemptService.onLoginFailure("admin")).thenReturn(0L);

            Result<?> result = authController.login(
                    loginRequest("admin", "bad-password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(401);
            // 凭证错误必须计入失败次数
            verify(loginAttemptService).onLoginFailure("admin");
        }

        @Test
        @DisplayName("凭证错误且达到锁定阈值时返回 423")
        void badCredentials_reachThreshold_returns423() {
            when(captchaService.verify(any(), any())).thenReturn(true);
            when(loginAttemptService.tryAcquireIp(anyString())).thenReturn(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("用户名或密码错误"));
            when(loginAttemptService.onLoginFailure("admin")).thenReturn(600000L);

            Result<?> result = authController.login(
                    loginRequest("admin", "bad-password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(423);
            assertThat(result.getMessage()).contains("锁定");
        }

        @Test
        @DisplayName("认证过程抛出其他异常时返回 401，且不记录登录失败")
        void unexpectedException_returns401_withoutFailureCount() {
            when(captchaService.verify(any(), any())).thenReturn(true);
            when(loginAttemptService.tryAcquireIp(anyString())).thenReturn(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new RuntimeException("认证服务不可用"));

            Result<?> result = authController.login(
                    loginRequest("admin", "password"), new MockHttpServletRequest(), new MockHttpServletResponse());

            assertThat(result.getCode()).isEqualTo(401);
            // catch Exception 分支不计入失败次数
            verify(loginAttemptService, never()).onLoginFailure(anyString());
        }

        @Test
        @DisplayName("多级代理场景下限流取 X-Forwarded-For 中的第一个 IP")
        void clientIp_takesFirstFromXForwardedFor() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            httpRequest.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
            when(captchaService.verify(any(), any())).thenReturn(true);
            // 仅对第一个 IP stub 为被限流，间接验证 getClientIp 的解析结果
            when(loginAttemptService.tryAcquireIp("1.2.3.4")).thenReturn(false);

            Result<?> result = authController.login(
                    loginRequest("admin", "password"), httpRequest, new MockHttpServletResponse());

            verify(loginAttemptService).tryAcquireIp("1.2.3.4");
            assertThat(result.getCode()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("refresh 刷新令牌")
    class Refresh {

        @Test
        @DisplayName("Cookie 与 X-Refresh-Token 头均缺失时抛出 BusinessException")
        void missingToken_throwsBusinessException() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest(); // 未设置任何 header
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();
            when(cookieUtils.getRefreshTokenFromRequest(httpRequest)).thenReturn(null);

            assertThatThrownBy(() -> authController.refresh(httpRequest, httpResponse))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("RefreshToken 缺失或已过期");
        }

        @Test
        @DisplayName("RefreshToken 无效时清除 refresh Cookie 并抛出 BusinessException")
        void invalidToken_clearsCookie_andThrows() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();
            when(cookieUtils.getRefreshTokenFromRequest(httpRequest)).thenReturn("bad-rt");
            when(jwtUtils.isValidRefreshToken("bad-rt")).thenReturn(false);

            assertThatThrownBy(() -> authController.refresh(httpRequest, httpResponse))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("RefreshToken 无效或已吊销，请重新登录");
            // 无效 Token 必须清除客户端残留的 Cookie
            verify(cookieUtils).clearRefreshCookie(httpResponse);
        }

        @Test
        @DisplayName("Cookie 缺失时回退到 X-Refresh-Token 请求头并刷新成功")
        void headerFallback_returns200() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            httpRequest.addHeader("X-Refresh-Token", "valid-rt");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();
            when(cookieUtils.getRefreshTokenFromRequest(httpRequest)).thenReturn(null);
            when(jwtUtils.isValidRefreshToken("valid-rt")).thenReturn(true);
            when(jwtUtils.getUsernameFromToken("valid-rt")).thenReturn("admin");
            when(jwtUtils.generateAccessToken("admin")).thenReturn("new-at");
            when(jwtUtils.generateRefreshToken("admin")).thenReturn("new-rt");

            Result<?> result = authController.refresh(httpRequest, httpResponse);

            assertThat(result.getCode()).isEqualTo(200);
            Map<String, Object> data = dataMap(result);
            assertThat(data)
                    .containsEntry("accessToken", "new-at")
                    .containsEntry("refreshToken", "new-rt");
            verify(jwtUtils).revokeRefreshToken("admin", "valid-rt");
            verify(cookieUtils).addRefreshCookie(httpResponse, "new-rt");
        }

        @Test
        @DisplayName("Cookie 中的有效 Token 走轮换逻辑：吊销旧 Token、签发并下发新 Token")
        void validCookieToken_rotated_returns200() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();
            when(cookieUtils.getRefreshTokenFromRequest(httpRequest)).thenReturn("old-rt");
            when(jwtUtils.isValidRefreshToken("old-rt")).thenReturn(true);
            when(jwtUtils.getUsernameFromToken("old-rt")).thenReturn("admin");
            when(jwtUtils.generateAccessToken("admin")).thenReturn("new-at");
            when(jwtUtils.generateRefreshToken("admin")).thenReturn("new-rt");

            Result<?> result = authController.refresh(httpRequest, httpResponse);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getMessage()).isEqualTo("刷新成功");
            Map<String, Object> data = dataMap(result);
            assertThat(data)
                    .containsEntry("accessToken", "new-at")
                    .containsEntry("refreshToken", "new-rt");
            // 轮换：旧 Token 必须被吊销，新 Token 通过 Cookie 下发
            verify(jwtUtils).revokeRefreshToken("admin", "old-rt");
            verify(cookieUtils).addRefreshCookie(httpResponse, "new-rt");
        }
    }

    @Nested
    @DisplayName("logout 登出")
    class Logout {

        @Test
        @DisplayName("携带 Bearer Token 登出：加入黑名单、吊销全部 refresh token、清除 Cookie")
        void withBearerToken_revokesEverything() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            httpRequest.addHeader("Authorization", "Bearer access-token");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();
            when(jwtUtils.getUsernameFromToken("access-token")).thenReturn("admin");

            Result<?> result = authController.logout(httpRequest, httpResponse);

            assertThat(result.getCode()).isEqualTo(200);
            verify(jwtUtils).addToBlacklist("access-token");
            verify(jwtUtils).revokeAllRefreshTokens("admin");
            verify(cookieUtils).clearRefreshCookie(httpResponse);
        }

        @Test
        @DisplayName("无 Authorization 头登出：不操作黑名单，仍清除 refresh Cookie")
        void withoutAuthorizationHeader_skipsBlacklist() {
            MockHttpServletRequest httpRequest = new MockHttpServletRequest();
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            Result<?> result = authController.logout(httpRequest, httpResponse);

            assertThat(result.getCode()).isEqualTo(200);
            verify(jwtUtils, never()).addToBlacklist(anyString());
            verify(cookieUtils).clearRefreshCookie(httpResponse);
        }
    }
}
