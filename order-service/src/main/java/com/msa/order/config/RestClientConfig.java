package com.msa.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient 빈 설정 클래스.
 */
@Configuration
public class RestClientConfig {

    /** Stock Service의 base URL. */
    @Value("${stock-service.url}")
    private String stockServiceUrl;

    /**
     * Stock Service 호출용 RestClient 빈을 생성한다.
     *
     * @return stock-service base URL이 설정된 RestClient 인스턴스
     */
    @Bean
    public RestClient stockRestClient() {
        return RestClient.builder()
            .baseUrl(stockServiceUrl)
            .build();
    }
}
