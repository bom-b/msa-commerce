package com.msa.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Order Service 애플리케이션 컨텍스트 로딩 테스트.
 */
@SpringBootTest
@TestPropertySource(
    properties = {
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    })
class OrderServiceApplicationTests {

    /**
     * Spring 애플리케이션 컨텍스트가 정상적으로 로딩되는지 검증한다.
     */
    @Test
    @DisplayName("애플리케이션 컨텍스트 로딩 성공")
    void contextLoads() {
    }
}
