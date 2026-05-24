package com.msa.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 프로퍼티.
 *
 * <p>{@code application.yml}의 {@code jwt.*} 항목을 바인딩한다.</p>
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /** HMAC-SHA256 서명에 사용할 비밀키. 256비트(32바이트) 이상을 권장한다. */
    private String secret;

    /** 토큰 만료 시간 (밀리초 단위). 기본값은 1시간(3600000ms). */
    private long expirationMs;
}
