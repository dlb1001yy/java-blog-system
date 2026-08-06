package com.dlbyy.blog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 双 Token 工具类
 * <p>
 * <ul>
 *     <li>AccessToken — 30 分钟，用于接口鉴权</li>
 *     <li>RefreshToken — 7 天，用于刷新 AccessToken</li>
 * </ul>
 * 支持 Redis 黑名单校验：登出时将 Token 加入黑名单，剩余有效期内自动失效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    /** AccessToken 有效期：30 分钟（毫秒） */
    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000L;

    /** RefreshToken 有效期：7 天（毫秒） */
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final RedisUtils redisUtils;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ==================== Token 生成 ====================

    /**
     * 生成 AccessToken（30 分钟）
     */
    public String generateAccessToken(String username) {
        return buildToken(username, ACCESS_TOKEN_EXPIRATION, TYPE_ACCESS);
    }

    /**
     * 生成 RefreshToken（7 天）
     */
    public String generateRefreshToken(String username) {
        return buildToken(username, REFRESH_TOKEN_EXPIRATION, TYPE_REFRESH);
    }

    private String buildToken(String username, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // ==================== Token 解析 ====================

    /**
     * 从 Token 中解析用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 Token 中解析 token_type
     */
    public String getTokenType(String token) {
        return parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * 获取 Token 过期时间（毫秒）
     */
    public long getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration().getTime();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ==================== Token 校验 ====================

    /**
     * 验证 Token 是否有效（签名 + 未过期 + 未在黑名单中）
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isBlacklisted(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证是否为 AccessToken
     */
    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(getTokenType(token));
    }

    /**
     * 验证是否为 RefreshToken
     */
    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(getTokenType(token));
    }

    // ==================== Redis 黑名单 ====================

    /**
     * 将 Token 加入黑名单（剩余有效期内自动过期）
     */
    public void addToBlacklist(String token) {
        long expiration = getExpirationFromToken(token);
        long remaining = expiration - System.currentTimeMillis();
        if (remaining > 0) {
            redisUtils.set(BLACKLIST_PREFIX + token, "1", remaining);
            log.info("Token 已加入黑名单，剩余有效期: {}ms", remaining);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return redisUtils.get(BLACKLIST_PREFIX + token) != null;
    }
}
