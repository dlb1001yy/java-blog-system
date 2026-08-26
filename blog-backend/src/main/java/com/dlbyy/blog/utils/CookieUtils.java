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
 * <p>
 * 前台与后台使用不同 Cookie 名（refresh_token / admin_refresh_token），
 * 避免同一浏览器前后台账号共用 Cookie 导致刷新令牌串号（后台拿到普通用户 token 报 403）。
 */
@Component
@RequiredArgsConstructor
public class CookieUtils {

    /** 前台/移动端 refresh Cookie */
    public static final String REFRESH_COOKIE_NAME = "refresh_token";
    /** 后台管理端 refresh Cookie（按端隔离，避免前后台互相覆盖） */
    public static final String ADMIN_REFRESH_COOKIE_NAME = "admin_refresh_token";

    private final SecurityProperties securityProperties;

    /**
     * 下发 refresh token Cookie（HttpOnly），前台端
     */
    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        addRefreshCookie(response, refreshToken, false);
    }

    /**
     * 下发 refresh token Cookie（HttpOnly），按端选择 Cookie 名
     */
    public void addRefreshCookie(HttpServletResponse response, String refreshToken, boolean admin) {
        addCookie(response, admin ? ADMIN_REFRESH_COOKIE_NAME : REFRESH_COOKIE_NAME, refreshToken);
    }

    /**
     * 清除 refresh token Cookie（登出 / 改密时调用），前台端
     */
    public void clearRefreshCookie(HttpServletResponse response) {
        clearRefreshCookie(response, false);
    }

    /**
     * 清除 refresh token Cookie，按端选择 Cookie 名
     */
    public void clearRefreshCookie(HttpServletResponse response, boolean admin) {
        addCookie(response, admin ? ADMIN_REFRESH_COOKIE_NAME : REFRESH_COOKIE_NAME, null);
    }

    /**
     * 从请求中读取指定名称的 refresh token Cookie
     */
    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        return getCookie(request, REFRESH_COOKIE_NAME);
    }

    /**
     * 从请求中读取后台端 refresh token Cookie
     */
    public String getAdminRefreshTokenFromRequest(HttpServletRequest request) {
        return getCookie(request, ADMIN_REFRESH_COOKIE_NAME);
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProperties.isCookieSecure());
        cookie.setPath("/api/auth");
        cookie.setMaxAge(value == null ? 0
                : (int) (securityProperties.getRefreshTokenDays() * 24L * 60 * 60));
        // SameSite 属性需通过响应头设置（Servlet Cookie API 无直接支持）
        String sameSite = securityProperties.getCookieSameSite() == null ? "None" : securityProperties.getCookieSameSite();
        response.addHeader("Set-Cookie",
                buildSetCookieHeader(cookie, sameSite));
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
