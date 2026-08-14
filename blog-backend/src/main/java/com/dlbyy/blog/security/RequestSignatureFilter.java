package com.dlbyy.blog.security;

import com.dlbyy.blog.properties.SignatureProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

/**
 * 请求签名校验过滤器
 * <p>
 * 通过校验 X-Timestamp / X-Nonce / X-Signature 请求头，防止 JWT 令牌被外部工具
 * （如 Apifox / Postman）拷贝后直接调用，同时防止重放攻击。
 * <p>
 * 签名算法：
 * <pre>
 * stringToSign = HTTP_METHOD + "\n" + REQUEST_URI + "\n" + TIMESTAMP + "\n" + NONCE
 * signature = HMAC-SHA256(secret, stringToSign) （Hex 小写）
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestSignatureFilter extends OncePerRequestFilter {

    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";

    private final SignatureProperties signatureProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!signatureProperties.isEnabled()) {
            return true;
        }
        String uri = request.getRequestURI();
        return !(uri.contains("/user/") || uri.contains("/admin/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String signature = request.getHeader(HEADER_SIGNATURE);

        // 1. 校验签名头是否齐全
        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            writeForbidden(response, "请求签名缺失，拒绝访问");
            return;
        }

        // 2. 校验时间戳是否在允许窗口内
        long requestTime;
        try {
            requestTime = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            writeForbidden(response, "请求签名验证失败");
            return;
        }
        long now = System.currentTimeMillis();
        long windowMillis = signatureProperties.getTimestampWindowSeconds() * 1000L;
        if (Math.abs(now - requestTime) > windowMillis) {
            writeForbidden(response, "请求已过期");
            return;
        }

        // 3. Nonce 防重放
        Boolean absent = stringRedisTemplate.opsForValue()
                .setIfAbsent("sign:nonce:" + nonce, "1",
                        signatureProperties.getTimestampWindowSeconds(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(absent)) {
            writeForbidden(response, "重复请求，拒绝访问");
            return;
        }

        // 4. 计算并比对签名
        String expectedSignature = computeSignature(
                signatureProperties.getSecret(),
                request.getMethod(),
                request.getRequestURI(),
                timestamp,
                nonce);
        if (!expectedSignature.equalsIgnoreCase(signature)) {
            writeForbidden(response, "请求签名验证失败");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 使用 HMAC-SHA256 计算签名（Hex 小写）
     */
    private String computeSignature(String secret, String method, String uri, String timestamp, String nonce) {
        try {
            String stringToSign = method + "\n" + uri + "\n" + timestamp + "\n" + nonce;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("计算请求签名失败", e);
            return "";
        }
    }

    /**
     * 写出 403 JSON 响应并终止过滤链
     */
    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + message
                + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
    }
}
