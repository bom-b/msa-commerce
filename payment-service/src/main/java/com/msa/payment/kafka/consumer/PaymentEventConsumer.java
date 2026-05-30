package com.msa.payment.kafka.consumer;

import com.msa.common.event.StockReservedEvent;
import com.msa.common.kafka.KafkaTopics;
import com.msa.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 결제 관련 Kafka 이벤트 구독자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    /** 결제 비즈니스 로직 서비스. */
    private final PaymentService paymentService;

    /**
     * {@code stock.reserved} 이벤트를 구독하여 결제를 처리한다.
     * 동일 orderId의 중복 이벤트는 DataIntegrityViolationException을 잡아 멱등성을 보장한다.
     *
     * @param event 재고 확보 완료 이벤트
     */
    @KafkaListener(topics = KafkaTopics.STOCK_RESERVED, groupId = "payment-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleStockReserved(StockReservedEvent event) {
        log.info("재고 확보 완료 이벤트 수신 - orderId: {}", event.orderId());
        try {
            paymentService.processPayment(event);
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 결제 처리 감지 - orderId: {} — skip", event.orderId());
        }
    }
}
