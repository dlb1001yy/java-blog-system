package com.dlbyy.blog.utils;

import com.dlbyy.blog.properties.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link JwtUtils} 单元测试。
 * <p>
 * 覆盖：Token 生成（类型/subject/有效期）、签名校验（合法/篡改/乱码/黑名单）、
 * RefreshToken 集合校验与吊销、黑名单写入与查询。
 * <p>
 * 说明：
 * <ul>
 *     <li>SecurityProperties 使用真实对象（15 分钟 / 7 天）；</li>
 *     <li>Redis 协作者（RedisUtils、StringRedisTemplate、SetOperations）全部 Mock；</li>
 *     <li>{@code secret} 字段由 @Value 注入、不在构造器中，通过 {@link ReflectionTestUtils} 反射注入。</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils 双 Token 工具类单元测试")
class JwtUtilsTest {

    /** HS512 要求密钥长度 ≥ 64 字节（512 位），此串为 78 个 ASCII 字符 */
    private static final String TEST_SECRET =
            "This-Is-A-Test-Secret-Key-For-HS512-Algorithm-Must-Be-At-Least-64-Bytes-Long!!";

    private static final String USERNAME = "alice";

    /** 用户 refresh token 集合键：jwt:refresh:{username} */
    private static final String REFRESH_KEY = "jwt:refresh:" + USERNAME;

    /** 黑名单键前缀 */
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /** AccessToken 有效期：15 分钟（毫秒） */
    private static final long ACCESS_EXPIRATION_MS = 15 * 60 * 1000L;

    /** RefreshToken 有效期：7 天（毫秒） */
    private static final long REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // 真实配置对象：AccessToken 15 分钟、RefreshToken 7 天
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.setAccessTokenMinutes(15);
        securityProperties.setRefreshTokenDays(7);
        // @RequiredArgsConstructor 构造参数顺序 = final 字段声明顺序：(SecurityProperties, RedisUtils, StringRedisTemplate)
        jwtUtils = new JwtUtils(securityProperties, redisUtils, stringRedisTemplate);
        // secret 不在构造器中，反射注入测试密钥
        ReflectionTestUtils.setField(jwtUtils, "secret", TEST_SECRET);
    }

    /**
     * 用测试密钥直接构造指定过期时间 / 类型的 Token（绕开 JwtUtils，便于构造"已过期"等特殊场景）
     */
    private String buildSignedToken(Date expiration, String tokenType) {
        return Jwts.builder()
                .setSubject(USERNAME)
                .claim("token_type", tokenType)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    /** 拼接黑名单键 */
    private String blacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }

    // ==================== 1. generateAccessToken ====================

    @Test
    @DisplayName("generateAccessToken：类型为 access、subject 往返一致、有效期约 15 分钟")
    void generateAccessToken_shouldBuildAccessTypedToken() {
        String token = jwtUtils.generateAccessToken(USERNAME);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.isAccessToken(token)).isTrue();
        assertThat(jwtUtils.isRefreshToken(token)).isFalse();
        // 用户名往返解析
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo(USERNAME);
        // 过期时间 ≈ 当前时间 + 15 分钟（允许 ±10 秒误差）
        assertThat(jwtUtils.getExpirationFromToken(token))
                .isCloseTo(System.currentTimeMillis() + ACCESS_EXPIRATION_MS, within(10_000L));
    }

    // ==================== 2. generateRefreshToken ====================

    @Test
    @DisplayName("generateRefreshToken：类型为 refresh、subject 正确、写入 Redis 集合并设置 7 天 TTL")
    void generateRefreshToken_shouldStoreTokenIntoRedisSetWithTtl() {
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);

        String token = jwtUtils.generateRefreshToken(USERNAME);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.isRefreshToken(token)).isTrue();
        assertThat(jwtUtils.isAccessToken(token)).isFalse();
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo(USERNAME);
        // Token 写入用户 refresh 集合
        verify(setOperations).add(REFRESH_KEY, token);
        // 为集合设置 TTL = RefreshToken 有效期（7 天，毫秒）
        verify(stringRedisTemplate).expire(REFRESH_KEY, REFRESH_EXPIRATION_MS, TimeUnit.MILLISECONDS);
    }

    // ==================== 3. validateToken ====================

    @Test
    @DisplayName("validateToken：合法且未拉黑的 Token 返回 true")
    void validateToken_validToken_shouldReturnTrue() {
        String token = jwtUtils.generateAccessToken(USERNAME);
        // 黑名单查询返回 null 表示未拉黑
        when(redisUtils.get(blacklistKey(token))).thenReturn(null);

        assertThat(jwtUtils.validateToken(token)).isTrue();
        verify(redisUtils).get(blacklistKey(token));
    }

    @Test
    @DisplayName("validateToken：签名被篡改的 Token 返回 false")
    void validateToken_tamperedToken_shouldReturnFalse() {
        String token = jwtUtils.generateAccessToken(USERNAME);
        // 将末尾 8 个字符替换为不同字符，破坏签名
        String tampered = token.substring(0, token.length() - 8) + "AAAAAAAA";

        assertThat(jwtUtils.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken：乱码字符串返回 false")
    void validateToken_garbageString_shouldReturnFalse() {
        assertThat(jwtUtils.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("validateToken：已加入黑名单的 Token 即使签名有效也返回 false")
    void validateToken_blacklistedToken_shouldReturnFalse() {
        String token = jwtUtils.generateAccessToken(USERNAME);
        // 黑名单命中
        when(redisUtils.get(blacklistKey(token))).thenReturn("1");

        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    // ==================== 4. isValidRefreshToken ====================

    @Test
    @DisplayName("isValidRefreshToken：签名有效且在用户 refresh 集合中返回 true")
    void isValidRefreshToken_memberOfRefreshSet_shouldReturnTrue() {
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        String token = jwtUtils.generateRefreshToken(USERNAME);
        when(setOperations.isMember(REFRESH_KEY, token)).thenReturn(true);

        assertThat(jwtUtils.isValidRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("isValidRefreshToken：不在用户 refresh 集合中（isMember=false）返回 false")
    void isValidRefreshToken_notMember_shouldReturnFalse() {
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        String token = jwtUtils.generateRefreshToken(USERNAME);
        when(setOperations.isMember(REFRESH_KEY, token)).thenReturn(false);

        assertThat(jwtUtils.isValidRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("isValidRefreshToken：isMember 返回 null（Redis 异常场景）返回 false")
    void isValidRefreshToken_nullMember_shouldReturnFalse() {
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        String token = jwtUtils.generateRefreshToken(USERNAME);
        when(setOperations.isMember(REFRESH_KEY, token)).thenReturn(null);

        assertThat(jwtUtils.isValidRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("isValidRefreshToken：传入 AccessToken 返回 false")
    void isValidRefreshToken_accessToken_shouldReturnFalse() {
        String accessToken = jwtUtils.generateAccessToken(USERNAME);

        assertThat(jwtUtils.isValidRefreshToken(accessToken)).isFalse();
    }

    @Test
    @DisplayName("isValidRefreshToken：传入无效 Token 返回 false")
    void isValidRefreshToken_invalidToken_shouldReturnFalse() {
        assertThat(jwtUtils.isValidRefreshToken("not.a.jwt")).isFalse();
    }

    // ==================== 5. 吊销 RefreshToken ====================

    @Test
    @DisplayName("revokeAllRefreshTokens：删除用户的 refresh 集合键")
    void revokeAllRefreshTokens_shouldDeleteRefreshSetKey() {
        jwtUtils.revokeAllRefreshTokens(USERNAME);

        verify(stringRedisTemplate).delete(REFRESH_KEY);
    }

    @Test
    @DisplayName("revokeRefreshToken：从用户 refresh 集合中移除指定 Token")
    void revokeRefreshToken_shouldRemoveTokenFromRefreshSet() {
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        String token = jwtUtils.generateRefreshToken(USERNAME);

        jwtUtils.revokeRefreshToken(USERNAME, token);

        verify(setOperations).remove(REFRESH_KEY, token);
    }

    // ==================== 6. addToBlacklist ====================

    @Test
    @DisplayName("addToBlacklist：未过期 Token 写入黑名单，TTL 为剩余有效期")
    void addToBlacklist_futureToken_shouldWriteBlacklistWithRemainingTtl() {
        // 构造 60 秒后才过期的 Token
        String token = buildSignedToken(new Date(System.currentTimeMillis() + 60_000L), "access");

        jwtUtils.addToBlacklist(token);

        // 写入黑名单，剩余有效期应落在 (0, 60000] 区间
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        verify(redisUtils).set(eq(blacklistKey(token)), eq("1"), ttlCaptor.capture());
        long ttl = ttlCaptor.getValue();
        assertThat(ttl).isPositive().isLessThanOrEqualTo(60_000L);
    }

    @Test
    @DisplayName("addToBlacklist：已过期 Token 不写黑名单")
    void addToBlacklist_expiredToken_shouldNotWriteBlacklist() {
        // 构造 60 秒前已过期的 Token
        String token = buildSignedToken(new Date(System.currentTimeMillis() - 60_000L), "access");

        // jjwt 0.11.5 解析已过期 Token 时会直接抛出 ExpiredJwtException（getExpirationFromToken 未捕获），
        // 无论内部是抛异常还是跳过写入，都不得对 Redis 产生任何写入
        catchThrowable(() -> jwtUtils.addToBlacklist(token));

        verify(redisUtils, never()).set(anyString(), any(), anyLong());
        verifyNoInteractions(redisUtils);
    }

    // ==================== 7. isBlacklisted ====================

    @Test
    @DisplayName("isBlacklisted：黑名单命中返回 true，未命中（null）返回 false")
    void isBlacklisted_shouldDependOnRedisValue() {
        String blacklisted = "token-in-blacklist";
        String normal = "token-not-in-blacklist";
        when(redisUtils.get(blacklistKey(blacklisted))).thenReturn("1");
        when(redisUtils.get(blacklistKey(normal))).thenReturn(null);

        assertThat(jwtUtils.isBlacklisted(blacklisted)).isTrue();
        assertThat(jwtUtils.isBlacklisted(normal)).isFalse();
    }
}
