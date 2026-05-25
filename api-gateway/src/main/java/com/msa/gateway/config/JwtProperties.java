package com.msa.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API Gateway의 JWT 설정 프로퍼티.
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT 서명 검증에 사용할 비밀키. Auth Service와 동일한 값으로 설정해야 한다.
     */
    private String secret;
}
