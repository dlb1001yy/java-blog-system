package com.dlbyy.blog.config;

import com.dlbyy.blog.properties.CorsProperties;
import com.dlbyy.blog.properties.SecurityProperties;
import com.dlbyy.blog.security.JwtAccessDeniedHandler;
import com.dlbyy.blog.security.JwtAuthenticationEntryPoint;
import com.dlbyy.blog.security.JwtAuthenticationFilter;
import com.dlbyy.blog.security.RequestSignatureFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final RequestSignatureFilter requestSignatureFilter;
    private final CorsProperties corsProperties;

    /** API 文档开关（swagger.enabled）：生产环境通过 SWAGGER_ENABLED=false 关闭文档相关放行 */
    @Value("${swagger.enabled:true}")
    private boolean swaggerEnabled;

    @Bean
    public PasswordEncoder passwordEncoder(SecurityProperties securityProperties) {
        // BCrypt 强度（盐值轮数）由 SecurityProperties 统一配置，默认 12 轮
        return new BCryptPasswordEncoder(securityProperties.getBcryptStrength());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/portal/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll();
                if (swaggerEnabled) {
                    // 仅在 API 文档开启时放行文档相关路径，生产环境（swagger.enabled=false）下直接拦截
                    auth.requestMatchers("/doc.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll();
                }
                auth.requestMatchers("/user/**").authenticated()
                    // 管理端接口仅允许 ADMIN 角色访问（CustomUserDetailsService 授予 ROLE_admin）
                    .requestMatchers("/admin/**").hasRole("admin")
                    .anyRequest().permitAll();
            })
            .addFilterBefore(requestSignatureFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 来源白名单：配置为 * 时按 Origin 模式匹配（本地开发兼容）；
        // 配置为精确域名列表时按精确来源匹配，未在白名单内的 Origin 一律不放行
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (allowedOrigins.contains("*")) {
            configuration.setAllowedOriginPatterns(allowedOrigins);
        } else {
            configuration.setAllowedOrigins(allowedOrigins);
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}