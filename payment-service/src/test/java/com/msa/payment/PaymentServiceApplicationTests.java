package com.msa.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/**
 * Payment Service 애플리케이션 컨텍스트 로딩 테스트.
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "stock.reserved",
        "payment.completed",
        "payment.failed"
    })
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:payment_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
    })
@DisplayName("PaymentServiceApplication 컨텍스트 로드 테스트")
class PaymentServiceApplicationTests {

    /**
     * EmbeddedKafka 브로커 주소를 Spring 컨텍스트에 동적으로 주입한다.
     *
     * @param registry 동적 속성 레지스트리
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.kafka.bootstrap-servers",
            () -> System.getProperty("spring.embedded.kafka.brokers", "localhost:9092"));
    }

    /**
     * Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 검증한다.
     */
    @Test
    @DisplayName("contextLoads: Spring 컨텍스트 정상 로드 검증")
    void contextLoads() {
    }
}
