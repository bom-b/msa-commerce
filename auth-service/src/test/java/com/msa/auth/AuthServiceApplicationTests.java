package com.msa.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 인증 서비스 Spring 컨텍스트 로드 테스트.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceApplicationTests {

    /**
     * Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인한다.
     */
    @Test
    @DisplayName("Spring 애플리케이션 컨텍스트 정상 로드")
    void contextLoads() {
    }
}
