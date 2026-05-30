package com.msa.payment.service;

import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import com.msa.common.event.StockReservedEvent;
import com.msa.payment.client.AuthServiceClient;
import com.msa.payment.domain.Payment;
import com.msa.payment.domain.PaymentStatus;
import com.msa.payment.dto.PaymentResponse;
import com.msa.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import static org.mockito.BDDMockito.willThrow;
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

    /** 목킹된 ApplicationEventPublisher. */
    @Mock
    private ApplicationEventPublisher eventPublisher;

    /** 목킹된 AuthServiceClient. */
    @Mock
    private AuthServiceClient authServiceClient;

    /**
     * stock.reserved 이벤트 수신 시 예치금 차감 후 결제 생성 및 payment.completed 이벤트 발행 테스트.
     */
    @Test
    @DisplayName("processPayment: 재고 확보 완료 이벤트 수신 시 결제 생성 및 completed 이벤트 발행")
    void processPayment_결제생성_및_이벤트발행() {
        // given
        StockReservedEvent event = new StockReservedEvent(UUID.randomUUID(), 1L, 1L, 10L, 2, new BigDecimal("20000.00"));

        given(paymentRepository.existsByOrderId(1L)).willReturn(false);

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
        then(authServiceClient).should().deduct(1L, new BigDecimal("20000.00"));
        then(paymentRepository).should().saveAndFlush(any(Payment.class));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(PaymentCompletedEvent.class);
        PaymentCompletedEvent published = (PaymentCompletedEvent) captor.getValue();
        assertThat(published.orderId()).isEqualTo(1L);
        assertThat(published.paymentId()).isEqualTo(100L);
        assertThat(published.amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
    }

    /**
     * 이미 처리된 orderId이면 결제 처리를 스킵하는 멱등성 테스트.
     */
    @Test
    @DisplayName("processPayment: 이미 처리된 orderId이면 결제 처리를 스킵한다")
    void processPayment_중복이벤트_스킵() {
        // given
        StockReservedEvent event = new StockReservedEvent(UUID.randomUUID(), 1L, 1L, 10L, 2, new BigDecimal("20000.00"));
        given(paymentRepository.existsByOrderId(1L)).willReturn(true);

        // when
        paymentService.processPayment(event);

        // then
        then(authServiceClient).should(never()).deduct(any(), any());
        then(paymentRepository).should(never()).saveAndFlush(any());
        then(eventPublisher).should(never()).publishEvent(any());
    }

    /**
     * 잔액 부족 시 Payment(FAILED) 저장 및 payment.failed 이벤트 발행 테스트.
     */
    @Test
    @DisplayName("processPayment: 잔액 부족 시 Payment(FAILED) 저장 및 payment.failed 이벤트 발행")
    void processPayment_잔액부족_실패처리() {
        // given
        StockReservedEvent event = new StockReservedEvent(UUID.randomUUID(), 1L, 1L, 10L, 2, new BigDecimal("20000.00"));
        given(paymentRepository.existsByOrderId(1L)).willReturn(false);
        willThrow(new IllegalArgumentException("잔액 부족: 현재 잔액=5000, 요청=20000.00"))
            .given(authServiceClient).deduct(1L, new BigDecimal("20000.00"));

        Payment failedPayment = Payment.builder()
            .orderId(1L)
            .amount(new BigDecimal("20000.00"))
            .status(PaymentStatus.FAILED)
            .createdAt(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(failedPayment, "id", 200L);
        given(paymentRepository.saveAndFlush(any(Payment.class))).willReturn(failedPayment);

        // when
        paymentService.processPayment(event);

        // then
        then(paymentRepository).should().saveAndFlush(any(Payment.class));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(PaymentFailedEvent.class);
        PaymentFailedEvent published = (PaymentFailedEvent) captor.getValue();
        assertThat(published.orderId()).isEqualTo(1L);
        assertThat(published.paymentId()).isEqualTo(200L);
        assertThat(published.amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
        assertThat(published.reason()).contains("잔액 부족");
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
