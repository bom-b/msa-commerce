package com.msa.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API Gateway의 JWT 설정 프로퍼티.
 *
 * <p>{@code application.yml}의 {@code jwt.*} 항목을 바인딩한다.
 * Auth Service와 동일한 비밀키를 사용해야 토큰 검증이 정상적으로 동작한다.</p>
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /** JWT 서명 검증에 사용할 비밀키. Auth Service와 동일한 값으로 설정해야 한다. */
    private String secret;
}
