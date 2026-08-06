package com.dlbyy.blog.security;

import com.dlbyy.blog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider（委托 {@link JwtUtils} 实现双 Token 生成与校验）
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUtils jwtUtils;

    /**
     * 生成 AccessToken（兼容旧调用，等价于 generateAccessToken）
     */
    public String generateToken(Authentication authentication) {
        return generateAccessToken(authentication);
    }

    /**
     * 生成 AccessToken（30 分钟）
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtils.generateAccessToken(userDetails.getUsername());
    }

    /**
     * 生成 RefreshToken（7 天）
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtils.generateRefreshToken(userDetails.getUsername());
    }

    public String getUsernameFromToken(String token) {
        return jwtUtils.getUsernameFromToken(token);
    }

    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }
}
