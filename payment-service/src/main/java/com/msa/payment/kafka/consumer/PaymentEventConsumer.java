package com.msa.payment.kafka.consumer;

import com.msa.common.event.OrderCreatedEvent;
import com.msa.common.kafka.KafkaTopics;
import com.msa.common.event.StockInsufficientEvent;
import com.msa.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 결제 관련 Kafka 이벤트 구독자. Order Service 및 Stock Service로부터 발행된 이벤트를 구독한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    /** 결제 비즈니스 로직 서비스. */
    private final PaymentService paymentService;

    /**
     * {@code order.created} 이벤트를 구독하여 결제를 처리한다.
     * 동일 orderId의 중복 이벤트는 DataIntegrityViolationException을 잡아 멱등성을 보장한다.
     *
     * @param event 주문 생성 이벤트
     */
    @KafkaListener(
        topics = KafkaTopics.ORDER_CREATED,
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}, eventId: {}", event.orderId(), event.eventId());
        try {
            paymentService.processPayment(event);
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 결제 처리 감지 - orderId: {}, eventId: {} — skip", event.orderId(), event.eventId());
        }
    }

    /**
     * {@code stock.insufficient} 이벤트를 구독하여 결제를 환불 처리한다.
     *
     * @param event 재고 부족 이벤트
     */
    @KafkaListener(
        topics = KafkaTopics.STOCK_INSUFFICIENT,
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleStockInsufficient(StockInsufficientEvent event) {
        log.info("재고 부족 이벤트 수신 - orderId: {}, eventId: {}", event.orderId(), event.eventId());
        paymentService.processRefund(event);
    }
}
