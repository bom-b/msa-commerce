package com.msa.payment.controller;

import com.msa.payment.domain.Payment;
import com.msa.payment.domain.PaymentStatus;
import com.msa.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PaymentController 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {"order.created", "payment.completed", "payment.failed"})
@DisplayName("PaymentController 통합 테스트")
class PaymentControllerTest {

    /** Testcontainers PostgreSQL 컨테이너 (클래스 공유). */
    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payment_db")
            .withUsername("postgres")
            .withPassword("postgres");

    /** MockMvc HTTP 요청 수행 객체. */
    @Autowired
    private MockMvc mockMvc;

    /** 테스트용 결제 레포지토리. */
    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Testcontainers 컨테이너 속성을 Spring 컨텍스트에 동적으로 등록한다.
     *
     * @param registry 동적 속성 레지스트리
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add(
            "spring.kafka.bootstrap-servers",
            () -> System.getProperty("spring.embedded.kafka.brokers", "localhost:9092"));
    }

    /**
     * 각 테스트 후 DB 데이터를 초기화한다.
     */
    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    /**
     * GET /payments/{orderId} 결제 조회 성공 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("GET /payments/{orderId}: 존재하는 주문 ID 조회 시 200 응답 및 결제 정보 반환")
    void GET_payments_orderId_조회성공() throws Exception {
        // given
        paymentRepository.save(
            Payment.builder()
                .orderId(1L)
                .amount(new BigDecimal("20000.00"))
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build());

        // when & then
        mockMvc.perform(get("/payments/{orderId}", 1L).header("X-User-Id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.orderId", is(1)))
            .andExpect(jsonPath("$.status", is("COMPLETED")))
            .andExpect(jsonPath("$.amount", is(20000.00)));
    }

    /**
     * GET /payments/{orderId} 존재하지 않는 주문 ID 조회 시 404 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("GET /payments/{orderId}: 존재하지 않는 주문 ID 조회 시 404 응답")
    void GET_payments_orderId_존재하지않는주문_404() throws Exception {
        // when & then
        mockMvc.perform(get("/payments/{orderId}", 999L).header("X-User-Id", "1"))
            .andExpect(status().isNotFound());
    }

    /**
     * GET /payments/{orderId} X-User-Id 헤더 누락 시 401 응답 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("GET /payments/{orderId}: X-User-Id 헤더 누락 시 401 응답")
    void GET_payments_orderId_XUserId헤더_누락_401() throws Exception {
        // when & then
        mockMvc.perform(get("/payments/{orderId}", 1L))
            .andExpect(status().isUnauthorized());
    }
}
