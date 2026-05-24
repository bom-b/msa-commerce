package com.msa.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 인증 서비스 Spring 컨텍스트 로드 테스트.
 *
 * <p>애플리케이션 컨텍스트가 오류 없이 정상적으로 시작되는지 검증한다.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceApplicationTests {

    /**
     * Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인한다.
     */
    @Test
    void contextLoads() {
    }
}
