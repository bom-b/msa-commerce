package com.msa.gateway;

import com.msa.gateway.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Cloud Gateway + WebFlux 기반 단일 진입점. JWT 인증 필터와 라우팅으로 각 마이크로서비스로 요청을 위임한다.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ApiGatewayApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * @param args 커맨드라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
