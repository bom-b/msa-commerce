package com.msa.stock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/** Stock Service 애플리케이션 컨텍스트 로드 테스트. */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class StockServiceApplicationTests {

    /** Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 검증한다. */
    @Test
    void contextLoads() {
    }
}
