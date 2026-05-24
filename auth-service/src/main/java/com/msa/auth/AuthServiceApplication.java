package com.msa.auth;

import com.msa.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 인증 서비스 애플리케이션 진입점.
 *
 * <p>JWT 토큰 발급 기능을 제공하는 마이크로서비스.
 * 학습 목적으로 {@code id=test / pw=test} 계정에 한해 JWT를 발급한다.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * @param args 커맨드라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
