package com.xhbookstore.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API模块安全配置 - 优先级高于框架默认配置
 * /api/mp/v1/** 路径由自定义JwtAuthenticationFilter处理，不走框架的认证流程
 */
@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/mp/v1/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/mp/v1/auth/wechat-phone-login").permitAll()
                .requestMatchers("/api/mp/v1/auth/session").permitAll()
                .requestMatchers("/api/mp/v1/**").permitAll() // JWT filter handles auth
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }
}
