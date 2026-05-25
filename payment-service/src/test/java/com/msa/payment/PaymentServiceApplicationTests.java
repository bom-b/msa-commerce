package com.msa.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * PaymentServiceApplication 컨텍스트 로드 테스트.
 * DB/Kafka 자동 설정을 제외하고 Spring 컨텍스트가 정상 로드되는지 검증한다.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@DisplayName("PaymentServiceApplication 컨텍스트 로드 테스트")
class PaymentServiceApplicationTests {

    /**
     * Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 검증한다.
     */
    @Test
    @DisplayName("contextLoads: Spring 컨텍스트 정상 로드 검증")
    void contextLoads() {
    }
}
