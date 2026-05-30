package com.msa.payment.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient 설정 클래스.
 */
@Configuration
public class RestClientConfig {

    /** Auth Service 기본 URL. */
    @Value("${auth-service.url}")
    private String authServiceUrl;

    /**
     * Auth Service용 RestClient 빈을 생성한다.
     *
     * @return authRestClient 인스턴스
     */
    @Bean
    @Qualifier("authRestClient")
    public RestClient authRestClient() {
        return RestClient.builder()
            .baseUrl(authServiceUrl)
            .build();
    }
}
