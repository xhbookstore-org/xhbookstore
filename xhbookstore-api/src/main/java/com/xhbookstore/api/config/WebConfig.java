package com.xhbookstore.api.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.xhbookstore.api.filter.JwtAuthenticationFilter;

/**
 * Web配置 - 注册Filter + 启用异步
 */
@Configuration
@EnableAsync
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter());
        registration.addUrlPatterns("/api/mp/v1/*");
        registration.setOrder(1);
        return registration;
    }
}
