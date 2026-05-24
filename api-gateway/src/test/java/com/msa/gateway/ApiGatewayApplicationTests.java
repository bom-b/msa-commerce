package com.msa.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * API Gateway Spring 컨텍스트 로드 테스트.
 *
 * <p>JWT 필터, 라우팅 설정 등 모든 빈이 오류 없이 초기화되는지 검증한다.</p>
 */
@SpringBootTest
class ApiGatewayApplicationTests {

    /**
     * Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인한다.
     */
    @Test
    void contextLoads() {
    }
}
