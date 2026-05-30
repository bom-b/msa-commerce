package com.msa.stock.kafka.consumer;

import com.msa.common.event.OrderCreatedEvent;
import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import com.msa.common.kafka.KafkaTopics;
import com.msa.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 재고 관련 Kafka 이벤트 구독자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventConsumer {

    /** 재고 비즈니스 로직 서비스. */
    private final StockService stockService;

    /**
     * {@code order.created} 이벤트를 구독하여 재고를 예약한다.
     *
     * @param event 주문 생성 이벤트
     */
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "stock-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}", event.orderId());
        try {
            stockService.reserveStock(event);
        } catch (DataIntegrityViolationException e) {
            log.warn("재고 중복 예약 감지 - orderId: {} — skip", event.orderId());
        }
    }

    /**
     * {@code payment.completed} 이벤트를 구독하여 재고 예약을 확정한다.
     *
     * @param event 결제 완료 이벤트
     */
    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "stock-service", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신 - orderId: {}", event.orderId());
        stockService.confirmReservation(event);
    }

    /**
     * {@code payment.failed} 이벤트를 구독하여 재고 예약을 취소하고 가용 재고를 복구한다.
     *
     * @param event 결제 실패 이벤트
     */
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "stock-service", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, reason: {}", event.orderId(), event.reason());
        stockService.releaseReservation(event);
    }
}
