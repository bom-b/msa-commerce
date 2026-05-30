package com.msa.order.kafka.consumer;

import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import com.msa.common.event.StockInsufficientEvent;
import com.msa.common.kafka.KafkaTopics;
import com.msa.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 주문 관련 Kafka 이벤트 구독자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

    /**
     * {@code payment.completed} 이벤트를 구독하여 주문을 완료 처리한다.
     *
     * @param event 결제 완료 이벤트
     */
    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "order-service", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신 - orderId: {}", event.orderId());
        orderService.completeOrder(event.orderId());
    }

    /**
     * {@code payment.failed} 이벤트를 구독하여 주문을 취소 처리한다.
     *
     * @param event 결제 실패 이벤트
     */
    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, reason: {}", event.orderId(), event.reason());
        orderService.cancelOrder(event.orderId(), event.reason());
    }

    /**
     * {@code stock.insufficient} 이벤트를 구독하여 주문을 취소 처리한다.
     *
     * @param event 재고 부족 이벤트
     */
    @KafkaListener(topics = KafkaTopics.STOCK_INSUFFICIENT, groupId = "order-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleStockInsufficient(StockInsufficientEvent event) {
        log.info("재고 부족 이벤트 수신 - orderId: {}, reason: {}", event.orderId(), event.reason());
        orderService.cancelOrder(event.orderId(), event.reason());
    }
}
