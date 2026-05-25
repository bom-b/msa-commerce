package com.msa.common.config;

import com.msa.common.auth.interceptor.AuthInterceptor;
import com.msa.common.auth.resolver.CurrentUserArgumentResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 공통 웹 MVC 설정. {@link AuthInterceptor}와 {@link CurrentUserArgumentResolver}를 자동으로 등록한다.
 *
 * <p>{@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}에
 * 등록되어 Spring Boot 자동 설정으로 활성화된다.
 */
@AutoConfiguration
public class CommonWebConfig implements WebMvcConfigurer {

    /**
     * 인증 인터셉터 빈.
     *
     * @return {@link AuthInterceptor} 인스턴스
     */
    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    /**
     * 현재 사용자 ArgumentResolver 빈.
     *
     * @return {@link CurrentUserArgumentResolver} 인스턴스
     */
    @Bean
    public CurrentUserArgumentResolver currentUserArgumentResolver() {
        return new CurrentUserArgumentResolver();
    }

    /**
     * {@link AuthInterceptor}를 인터셉터 레지스트리에 등록한다.
     *
     * @param registry 인터셉터 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor());
    }

    /**
     * {@link CurrentUserArgumentResolver}를 ArgumentResolver 목록에 등록한다.
     *
     * @param resolvers ArgumentResolver 목록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver());
    }
}
