package com.msa.stock.kafka.producer;

import com.msa.common.event.StockInsufficientEvent;
import com.msa.common.event.StockReservedEvent;
import com.msa.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * 재고 관련 Kafka 이벤트 발행자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProducer {

    /** Kafka 메시지 발행 템플릿. */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 재고 확보 완료 이벤트를 {@code stock.reserved} 토픽으로 발행한다.
     *
     * @param event 발행할 재고 확보 완료 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendStockReserved(StockReservedEvent event) {
        log.info("재고 확보 완료 이벤트 발행 - orderId: {}", event.orderId());
        kafkaTemplate.send(KafkaTopics.STOCK_RESERVED, String.valueOf(event.orderId()), event);
    }

    /**
     * 재고 부족 이벤트를 {@code stock.insufficient} 토픽으로 발행한다.
     *
     * @param event 발행할 재고 부족 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendStockInsufficient(StockInsufficientEvent event) {
        log.info("재고 부족 이벤트 발행 - orderId: {}, reason: {}", event.orderId(), event.reason());
        kafkaTemplate.send(KafkaTopics.STOCK_INSUFFICIENT, String.valueOf(event.orderId()), event);
    }
}
