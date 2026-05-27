package com.msa.order.kafka.consumer;

import com.msa.order.config.KafkaConfig;
import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import com.msa.common.event.StockInsufficientEvent;
import com.msa.common.event.StockReservedEvent;
import com.msa.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 주문 관련 Kafka 이벤트 구독자. Payment Service 및 Stock Service로부터 발행된 이벤트를 구독하여
 * 주문 상태를 업데이트한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    /**
     * 주문 비즈니스 로직 서비스.
     */
    private final OrderService orderService;

    /**
     * {@code payment.completed} 이벤트를 구독한다. 결제 완료 이벤트 수신 시 별도 처리 없음.
     * stock.reserved 이벤트 수신 후 COMPLETED로 전이한다.
     *
     * @param event 결제 완료 이벤트
     */
    @KafkaListener(
        topics = KafkaConfig.PAYMENT_COMPLETED_TOPIC,
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info(
            "결제 완료 이벤트 수신 - orderId: {}, eventId: {}", event.orderId(), event.eventId());
        // stock.reserved 이벤트 수신 시 COMPLETED로 전이하므로 여기서는 로그만 기록
    }

    /**
     * {@code payment.failed} 이벤트를 구독하여 주문을 취소 처리한다.
     *
     * @param event 결제 실패 이벤트
     */
    @KafkaListener(
        topics = KafkaConfig.PAYMENT_FAILED_TOPIC,
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info(
            "결제 실패 이벤트 수신 - orderId: {}, reason: {}, eventId: {}",
            event.orderId(),
            event.reason(),
            event.eventId());
        orderService.cancelOrder(event.orderId());
    }

    /**
     * {@code stock.reserved} 이벤트를 구독하여 주문을 완료 처리한다.
     *
     * @param event 재고 확보 완료 이벤트
     */
    @KafkaListener(
        topics = KafkaConfig.STOCK_RESERVED_TOPIC,
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleStockReserved(StockReservedEvent event) {
        log.info(
            "재고 확보 완료 이벤트 수신 - orderId: {}, productId: {}, eventId: {}",
            event.orderId(),
            event.productId(),
            event.eventId());
        orderService.completeOrder(event.orderId());
    }

    /**
     * {@code stock.insufficient} 이벤트를 구독하여 주문을 취소 처리한다.
     *
     * @param event 재고 부족 이벤트
     */
    @KafkaListener(
        topics = KafkaConfig.STOCK_INSUFFICIENT_TOPIC,
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleStockInsufficient(StockInsufficientEvent event) {
        log.info(
            "재고 부족 이벤트 수신 - orderId: {}, reason: {}, eventId: {}",
            event.orderId(),
            event.reason(),
            event.eventId());
        orderService.cancelOrder(event.orderId());
    }
}
