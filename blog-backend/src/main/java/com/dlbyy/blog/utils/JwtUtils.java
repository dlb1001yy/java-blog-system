package com.dlbyy.blog.utils;

import com.dlbyy.blog.properties.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 双 Token 工具类
 * <p>
 * <ul>
 *     <li>AccessToken — 默认 15 分钟（可配置），用于接口鉴权</li>
 *     <li>RefreshToken — 默认 7 天（可配置），用于刷新 AccessToken</li>
 * </ul>
 * 支持 Redis 黑名单校验：登出/改密/锁定时将 Token 加入黑名单，剩余有效期内自动失效。
 * 黑名单同时覆盖 AccessToken 与 RefreshToken（含 jti 维度，便于主动吊销）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    private final SecurityProperties securityProperties;

    /** AccessToken 有效期（毫秒），由 security.login.access-token-minutes 配置，默认 15 分钟 */
    private long getAccessTokenExpiration() {
        return (long) securityProperties.getAccessTokenMinutes() * 60 * 1000L;
    }

    /** RefreshToken 有效期（毫秒），由 security.login.refresh-token-days 配置，默认 7 天 */
    private long getRefreshTokenExpiration() {
        return (long) securityProperties.getRefreshTokenDays() * 24 * 60 * 60 * 1000L;
    }

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    /** 记录某用户当前有效的 refresh token（用于登出/改密时主动吊销） */
    private static final String REFRESH_SET_PREFIX = "jwt:refresh:";

    private final RedisUtils redisUtils;
    private final StringRedisTemplate stringRedisTemplate;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ==================== Token 生成 ====================

    /**
     * 生成 AccessToken（默认 15 分钟）
     */
    public String generateAccessToken(String username) {
        return buildToken(username, getAccessTokenExpiration(), TYPE_ACCESS);
    }

    /**
     * 生成 RefreshToken（默认 7 天），并记录到 Redis 集合，便于登出/改密时主动吊销
     */
    public String generateRefreshToken(String username) {
        String token = buildToken(username, getRefreshTokenExpiration(), TYPE_REFRESH);
        stringRedisTemplate.opsForSet().add(REFRESH_SET_PREFIX + username, token);
        return token;
    }

    /**
     * 校验 refresh token 是否仍有效（签名有效 + 未过期 + 未被吊销）。
     * 吊销场景：1) 加入黑名单；2) 不在该用户有效 refresh 集合中（登出/改密后旧 token 失效）。
     */
    public boolean isValidRefreshToken(String token) {
        if (!validateToken(token) || !isRefreshToken(token)) {
            return false;
        }
        String username = getUsernameFromToken(token);
        Boolean member = stringRedisTemplate.opsForSet().isMember(REFRESH_SET_PREFIX + username, token);
        return Boolean.TRUE.equals(member);
    }

    /**
     * 吊销某用户所有 refresh token（登出 / 改密 / 锁定时调用）
     */
    public void revokeAllRefreshTokens(String username) {
        stringRedisTemplate.delete(REFRESH_SET_PREFIX + username);
    }

    /**
     * 吊销指定 refresh token
     */
    public void revokeRefreshToken(String username, String token) {
        stringRedisTemplate.opsForSet().remove(REFRESH_SET_PREFIX + username, token);
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
