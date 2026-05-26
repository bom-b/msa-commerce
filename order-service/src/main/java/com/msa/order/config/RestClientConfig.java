package com.msa.order.config;

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

    /**
     * 재고 서비스 URL.
     */
    @Value("${stock-service.url}")
    private String stockServiceUrl;

    /**
     * stockRestClient Bean을 생성한다.
     *
     * @return stockRestClient 인스턴스
     */
    @Bean
    @Qualifier("stockRestClient")
    public RestClient stockRestClient() {
        return RestClient.builder()
            .baseUrl(stockServiceUrl)
            .build();
    }
}
