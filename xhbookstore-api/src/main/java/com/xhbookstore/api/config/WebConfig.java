package com.xhbookstore.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.xhbookstore.api.filter.IdempotencyResponseCacheFilter;
import com.xhbookstore.api.filter.JwtAuthenticationFilter;
import com.xhbookstore.api.interceptor.ApiIdentityInterceptor;
import com.xhbookstore.api.interceptor.IdempotencyInterceptor;

/**
 * Web配置 - 注册Filter + 启用异步
 */
@Configuration
@EnableAsync
public class WebConfig implements WebMvcConfigurer {

    @Autowired private SecurityProperties securityProperties;
    @Autowired private ApiIdentityInterceptor apiIdentityInterceptor;
    @Autowired private IdempotencyInterceptor idempotencyInterceptor;
    @Autowired private RedisTemplate<Object, Object> redisTemplate;

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(securityProperties.getJwt().getSecret()));
        registration.addUrlPatterns("/api/mp/v1/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<IdempotencyResponseCacheFilter> idempotencyResponseCacheFilterRegistration() {
        FilterRegistrationBean<IdempotencyResponseCacheFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IdempotencyResponseCacheFilter(redisTemplate));
        registration.addUrlPatterns("/api/mp/v1/*");
        registration.setOrder(2);
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiIdentityInterceptor)
                .addPathPatterns("/api/mp/v1/**")
                .order(1);
        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/api/mp/v1/**")
                .order(2);
    }
}
