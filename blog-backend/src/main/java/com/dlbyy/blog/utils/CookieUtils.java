package com.dlbyy.blog.utils;

import com.dlbyy.blog.properties.SecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Refresh Token HTTP-only Cookie 工具。
 * 刷新令牌通过 HttpOnly + Secure + SameSite 下发，前端无法用 JS 读取，防 XSS 窃取。
 */
@Component
@RequiredArgsConstructor
public class CookieUtils {

    public static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final SecurityProperties securityProperties;

    /**
     * 下发 refresh token Cookie（HttpOnly）
     */
    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProperties.isCookieSecure());
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (securityProperties.getRefreshTokenDays() * 24L * 60 * 60));
        // SameSite 属性需通过响应头设置（Servlet Cookie API 无直接支持）
        String sameSite = securityProperties.getCookieSameSite() == null ? "None" : securityProperties.getCookieSameSite();
        response.addHeader("Set-Cookie",
                buildSetCookieHeader(cookie, sameSite));
    }

    /**
     * 清除 refresh token Cookie（登出 / 改密时调用）
     */
    public void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProperties.isCookieSecure());
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        String sameSite = securityProperties.getCookieSameSite() == null ? "None" : securityProperties.getCookieSameSite();
        response.addHeader("Set-Cookie", buildSetCookieHeader(cookie, sameSite));
    }

    /**
     * 从请求中读取 refresh token Cookie
     */
    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String buildSetCookieHeader(Cookie cookie, String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append("=");
        if (cookie.getValue() != null) {
            sb.append(cookie.getValue());
        }
        sb.append("; Path=").append(cookie.getPath());
        sb.append("; Max-Age=").append(cookie.getMaxAge());
        sb.append("; HttpOnly");
        if (cookie.getSecure()) {
            sb.append("; Secure");
        }
        if (sameSite != null && !sameSite.isBlank()) {
            sb.append("; SameSite=").append(sameSite);
        }
        return sb.toString();
    }
}
