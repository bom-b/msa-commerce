package com.msa.order.kafka.producer;

import com.msa.common.event.OrderCreatedEvent;
import com.msa.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 주문 관련 Kafka 이벤트 발행자. 주문 생성 후 {@code order.created} 토픽에 이벤트를 발행하여
 * Payment Service의 결제 처리를 트리거한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    /**
     * Kafka 메시지 발행 템플릿.
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 주문 생성 이벤트를 {@code order.created} 토픽으로 발행한다.
     *
     * @param event 발행할 주문 생성 이벤트
     */
    public void sendOrderCreated(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 발행 - orderId: {}, eventId: {}", event.orderId(), event.eventId());
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, String.valueOf(event.orderId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("주문 생성 이벤트 발행 실패 - orderId: {}, error: {}", event.orderId(), ex.getMessage());
                } else {
                    log.info("주문 생성 이벤트 발행 성공 - orderId: {}, offset: {}", event.orderId(), result.getRecordMetadata().offset());
                }
            });
    }
}
