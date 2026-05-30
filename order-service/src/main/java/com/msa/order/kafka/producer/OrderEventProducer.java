package com.msa.order.kafka.producer;

import com.msa.common.event.OrderCreatedEvent;
import com.msa.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * 주문 관련 Kafka 이벤트 발행자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 주문 생성 이벤트를 {@code order.created} 토픽으로 발행한다.
     *
     * @param event 발행할 주문 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 발행 - orderId: {}", event.orderId());
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, String.valueOf(event.orderId()), event);
    }
}
