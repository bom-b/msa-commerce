package com.msa.payment.service;

import com.msa.payment.domain.Payment;
import com.msa.payment.domain.PaymentStatus;
import com.msa.payment.dto.PaymentResponse;
import com.msa.payment.dto.event.OrderCreatedEvent;
import com.msa.payment.dto.event.PaymentCompletedEvent;
import com.msa.payment.dto.event.PaymentFailedEvent;
import com.msa.payment.dto.event.StockInsufficientEvent;
import com.msa.payment.kafka.producer.PaymentEventProducer;
import com.msa.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * PaymentService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    /** 테스트 대상 PaymentService. */
    @InjectMocks
    private PaymentService paymentService;

    /** 목킹된 PaymentRepository. */
    @Mock
    private PaymentRepository paymentRepository;

    /** 목킹된 PaymentEventProducer. */
    @Mock
    private PaymentEventProducer paymentEventProducer;

    /**
     * order.created 이벤트 수신 시 결제 생성 및 payment.completed 이벤트 발행 테스트.
     * 트랜잭션 컨텍스트가 없는 단위 테스트에서는 Kafka 이벤트가 즉시 발행된다.
     */
    @Test
    @DisplayName("processPayment: 정상 주문 이벤트 수신 시 결제 생성 및 completed 이벤트 발행")
    void processPayment_결제생성_및_이벤트발행() {
        // given
        OrderCreatedEvent event = new OrderCreatedEvent(
            UUID.randomUUID(), 1L, "user1", 10L, 2, new BigDecimal("20000.00"));

        Payment savedPayment = Payment.builder()
            .orderId(1L)
            .amount(new BigDecimal("20000.00"))
            .status(PaymentStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(savedPayment, "id", 100L);

        given(paymentRepository.saveAndFlush(any(Payment.class))).willReturn(savedPayment);

        // when
        paymentService.processPayment(event);

        // then
        then(paymentRepository).should().saveAndFlush(any(Payment.class));

        ArgumentCaptor<PaymentCompletedEvent> captor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        then(paymentEventProducer).should().sendPaymentCompleted(captor.capture());

        PaymentCompletedEvent published = captor.getValue();
        assertThat(published.orderId()).isEqualTo(1L);
        assertThat(published.paymentId()).isEqualTo(100L);
        assertThat(published.amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
    }

    /**
     * 중복 orderId 삽입 시 DataIntegrityViolationException이 전파되어 소비자 계층에서 처리됨을 검증한다.
     */
    @Test
    @DisplayName("processPayment: 중복 orderId 삽입 시 DataIntegrityViolationException 전파")
    void processPayment_중복이벤트_DataIntegrityViolationException_전파() {
        // given
        OrderCreatedEvent event = new OrderCreatedEvent(
            UUID.randomUUID(), 1L, "user1", 10L, 2, new BigDecimal("20000.00"));

        given(paymentRepository.saveAndFlush(any(Payment.class))).willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(event))
            .isInstanceOf(DataIntegrityViolationException.class);
        then(paymentEventProducer).should(never()).sendPaymentCompleted(any());
    }

    /**
     * stock.insufficient 이벤트 수신 시 결제 REFUNDED 전이 및 payment.failed 이벤트 발행 테스트.
     */
    @Test
    @DisplayName("processRefund: 재고 부족 이벤트 수신 시 결제 REFUNDED 전이 및 failed 이벤트 발행")
    void processRefund_환불처리_및_이벤트발행() {
        // given
        StockInsufficientEvent event = new StockInsufficientEvent(
            UUID.randomUUID(), 1L, 10L, 2, "재고 부족");

        Payment payment = Payment.builder()
            .orderId(1L)
            .amount(new BigDecimal("20000.00"))
            .status(PaymentStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(payment, "id", 100L);

        given(paymentRepository.findByOrderId(1L)).willReturn(Optional.of(payment));

        // when
        paymentService.processRefund(event);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        then(paymentEventProducer).should().sendPaymentFailed(captor.capture());

        PaymentFailedEvent published = captor.getValue();
        assertThat(published.orderId()).isEqualTo(1L);
        assertThat(published.paymentId()).isEqualTo(100L);
        assertThat(published.reason()).isEqualTo("재고 부족");
    }

    /**
     * 환불 대상 결제가 없을 때 NoSuchElementException이 발생하는 테스트.
     */
    @Test
    @DisplayName("processRefund: 결제 레코드 없을 시 NoSuchElementException 발생")
    void processRefund_결제없음_예외발생() {
        // given
        StockInsufficientEvent event = new StockInsufficientEvent(
            UUID.randomUUID(), 999L, 10L, 2, "재고 부족");

        given(paymentRepository.findByOrderId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.processRefund(event))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessageContaining("999");
    }

    /**
     * 주문 ID로 결제 조회 성공 테스트.
     */
    @Test
    @DisplayName("getPaymentByOrderId: 존재하는 주문 ID 조회 시 PaymentResponse 반환")
    void getPaymentByOrderId_조회성공() {
        // given
        Payment payment = Payment.builder()
            .orderId(1L)
            .amount(new BigDecimal("20000.00"))
            .status(PaymentStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(payment, "id", 100L);

        given(paymentRepository.findByOrderId(1L)).willReturn(Optional.of(payment));

        // when
        PaymentResponse response = paymentService.getPaymentByOrderId(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
    }

    /**
     * 존재하지 않는 주문 ID로 조회 시 NoSuchElementException 발생 테스트.
     */
    @Test
    @DisplayName("getPaymentByOrderId: 존재하지 않는 주문 ID 조회 시 NoSuchElementException 발생")
    void getPaymentByOrderId_존재하지않는주문_예외발생() {
        // given
        given(paymentRepository.findByOrderId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(999L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessageContaining("999");
    }
}
