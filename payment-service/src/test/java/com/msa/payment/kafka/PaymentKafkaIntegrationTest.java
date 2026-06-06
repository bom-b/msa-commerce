package com.msa.payment.kafka;

import com.msa.common.event.StockReservedEvent;
import com.msa.payment.client.AuthServiceClient;
import com.msa.payment.domain.PaymentStatus;
import com.msa.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Payment Service Kafka 통합 테스트.
 * stock.reserved 이벤트 발행 후 결제 레코드 상태를 검증한다.
 */
@SpringBootTest
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {"stock.reserved", "payment.completed", "payment.failed"})
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

    /** Auth Service 클라이언트 목킹 (실제 HTTP 호출 방지). */
    @MockBean
    private AuthServiceClient authServiceClient;

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
     * stock.reserved 이벤트 수신 시 결제 레코드가 COMPLETED 상태로 생성되는지 검증한다.
     * AuthServiceClient는 MockBean으로 대체하여 void 반환(아무 동작 없음)으로 처리된다.
     *
     * @throws Exception Kafka 발행 중 발생할 수 있는 예외
     */
    @Test
    @DisplayName("stock.reserved 이벤트 수신 후 결제 레코드 COMPLETED 상태로 생성")
    void stockReserved_onEventReceived_createsCompletedPayment() {
        // given
        Long orderId = 42L;
        StockReservedEvent event = new StockReservedEvent(
            UUID.randomUUID(), orderId, 1L, 10L, 2, 30000L);

        // when
        kafkaTemplate.send("stock.reserved", String.valueOf(orderId), event);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(paymentRepository.existsByOrderId(orderId)).isTrue();
            paymentRepository.findByOrderIdAndUserId(orderId, 1L).ifPresent(payment -> {
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                assertThat(payment.getAmount()).isEqualTo(30000L);
                assertThat(payment.getUserId()).isEqualTo(1L);
            });
        });
    }

    /**
     * 동일 orderId로 stock.reserved 이벤트를 두 번 발행할 때 결제 레코드가 1건만 생성되는 멱등성을 검증한다.
     */
    @Test
    @DisplayName("stock.reserved 이벤트 중복 수신 시 결제 레코드 1건만 생성 (멱등성)")
    void stockReserved_onDuplicateEvent_createsSinglePayment() {
        // given
        Long orderId = 99L;
        StockReservedEvent event = new StockReservedEvent(
            UUID.randomUUID(), orderId, 1L, 10L, 1, 10000L);

        // when
        kafkaTemplate.send("stock.reserved", String.valueOf(orderId), event);
        kafkaTemplate.send("stock.reserved", String.valueOf(orderId), event);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .count();
            assertThat(count).isEqualTo(1);
        });
    }
}
