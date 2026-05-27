package com.msa.payment.kafka;

import com.msa.payment.domain.PaymentStatus;
import com.msa.common.event.OrderCreatedEvent;
import com.msa.common.event.StockInsufficientEvent;
import com.msa.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Payment Service Kafka 통합 테스트.
 * order.created / stock.insufficient 이벤트 발행 후 결제 레코드 상태를 검증한다.
 */
@SpringBootTest
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {"order.created", "payment.completed", "payment.failed", "stock.insufficient"})
@DisplayName("Payment Service Kafka 통합 테스트")
class PaymentKafkaIntegrationTest {

    /** Testcontainers PostgreSQL 컨테이너 (클래스 공유). */
    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payment_db")
            .withUsername("postgres")
            .withPassword("postgres");

    /** Kafka 메시지 발행 템플릿. */
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /** 결제 데이터 검증용 레포지토리. */
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
     * order.created 이벤트 발행 시 결제 레코드가 COMPLETED 상태로 생성되는지 검증한다.
     */
    @Test
    @DisplayName("order.created 이벤트 수신 후 결제 레코드 COMPLETED 상태로 생성")
    void orderCreated_이벤트_수신_결제_생성() {
        // given
        Long orderId = 42L;
        OrderCreatedEvent event = new OrderCreatedEvent(
            UUID.randomUUID(), orderId, 1L, 10L, 2, new BigDecimal("30000.00"));

        // when
        kafkaTemplate.send("order.created", String.valueOf(orderId), event);

        // then — 컨슈머가 비동기로 처리하므로 Awaitility로 폴링
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(paymentRepository.existsByOrderId(orderId)).isTrue();
            paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("30000.00"));
            });
        });
    }

    /**
     * 동일 orderId로 order.created 이벤트를 두 번 발행할 때 결제 레코드가 1건만 생성되는 멱등성을 검증한다.
     */
    @Test
    @DisplayName("order.created 이벤트 중복 수신 시 결제 레코드 1건만 생성 (멱등성)")
    void orderCreated_이벤트_중복_멱등성() {
        // given
        Long orderId = 99L;
        OrderCreatedEvent event = new OrderCreatedEvent(
            UUID.randomUUID(), orderId, 1L, 10L, 1, new BigDecimal("10000.00"));

        // when — 동일 이벤트를 두 번 발행
        kafkaTemplate.send("order.created", String.valueOf(orderId), event);
        kafkaTemplate.send("order.created", String.valueOf(orderId), event);

        // then — 두 메시지 처리 시간 여유를 두고 count가 정확히 1인지 untilAsserted 블록 내에서 검증
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .count();
            assertThat(count).isEqualTo(1);
        });
    }

    /**
     * stock.insufficient 이벤트 수신 시 결제가 REFUNDED 상태로 전이되는지 검증한다.
     */
    @Test
    @DisplayName("stock.insufficient 이벤트 수신 후 결제 REFUNDED 상태로 전이")
    void stockInsufficient_이벤트_수신_결제_환불() {
        // given — 먼저 결제 레코드가 존재해야 함
        Long orderId = 77L;
        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
            UUID.randomUUID(), orderId, 1L, 10L, 5, new BigDecimal("50000.00"));
        kafkaTemplate.send("order.created", String.valueOf(orderId), orderEvent);

        // 결제 생성 완료 대기
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(paymentRepository.existsByOrderId(orderId)).isTrue()
        );

        // when — stock.insufficient 이벤트 발행
        StockInsufficientEvent stockEvent = new StockInsufficientEvent(
            UUID.randomUUID(), orderId, 10L, 5, "재고 부족");
        kafkaTemplate.send("stock.insufficient", String.valueOf(orderId), stockEvent);

        // then — REFUNDED 상태로 전이 검증
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
            paymentRepository.findByOrderId(orderId).ifPresent(payment ->
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED)
            )
        );
    }
}
