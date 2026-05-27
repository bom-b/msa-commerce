package com.msa.payment.service;

import com.msa.payment.domain.Payment;
import com.msa.payment.domain.PaymentStatus;
import com.msa.payment.dto.PaymentResponse;
import com.msa.common.event.OrderCreatedEvent;
import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import com.msa.common.event.StockInsufficientEvent;
import com.msa.payment.kafka.producer.PaymentEventProducer;
import com.msa.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 결제 비즈니스 로직 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    /** 결제 데이터 접근 레포지토리. */
    private final PaymentRepository paymentRepository;

    /** 결제 이벤트 Kafka 발행자. */
    private final PaymentEventProducer paymentEventProducer;

    /**
     * order.created 이벤트를 처리하여 결제를 생성하고 payment.completed 이벤트를 발행한다.
     * DB 커밋 완료 후 Kafka 이벤트를 발행하여 데이터 정합성을 보장한다.
     * 중복 orderId 삽입 시 DataIntegrityViolationException이 전파되며, 소비자 계층에서 처리한다.
     *
     * @param event 수신된 주문 생성 이벤트
     */
    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        Payment payment = Payment.builder()
            .orderId(event.orderId())
            .amount(event.totalAmount())
            .status(PaymentStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();

        Payment savedPayment = paymentRepository.saveAndFlush(payment);
        log.info("결제 생성 완료 - paymentId: {}, orderId: {}", savedPayment.getId(), savedPayment.getOrderId());

        PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
            UUID.randomUUID(),
            savedPayment.getOrderId(),
            savedPayment.getId(),
            savedPayment.getAmount());

        publishAfterCommit(() -> paymentEventProducer.sendPaymentCompleted(completedEvent));
    }

    /**
     * stock.insufficient 이벤트를 처리하여 결제를 REFUNDED 상태로 전이하고 payment.failed 이벤트를 발행한다.
     *
     * @param event 수신된 재고 부족 이벤트
     * @throws NoSuchElementException 해당 주문 ID의 결제 레코드가 없을 경우
     */
    @Transactional
    public void processRefund(StockInsufficientEvent event) {
        Payment payment = paymentRepository.findByOrderId(event.orderId())
            .orElseThrow(() -> new NoSuchElementException("환불할 결제를 찾을 수 없습니다. orderId: " + event.orderId()));

        payment.updateStatus(PaymentStatus.REFUNDED);
        log.info("결제 환불 처리 - paymentId: {}, orderId: {}", payment.getId(), payment.getOrderId());

        PaymentFailedEvent failedEvent = new PaymentFailedEvent(
            UUID.randomUUID(),
            payment.getOrderId(),
            payment.getId(),
            payment.getAmount(),
            event.reason());

        publishAfterCommit(() -> paymentEventProducer.sendPaymentFailed(failedEvent));
    }

    /**
     * 주문 ID로 결제를 조회한다.
     *
     * @param orderId 조회할 주문 ID
     * @return 결제 응답 DTO
     * @throws NoSuchElementException 해당 주문 ID의 결제가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NoSuchElementException("결제를 찾을 수 없습니다. orderId: " + orderId));
        return PaymentResponse.from(payment);
    }

    /**
     * 활성 트랜잭션이 있으면 커밋 완료 후 publisher를 실행하고, 없으면 즉시 실행한다.
     *
     * @param publisher 실행할 작업
     */
    private void publishAfterCommit(Runnable publisher) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.run();
                }
            });
        } else {
            publisher.run();
        }
    }
}
