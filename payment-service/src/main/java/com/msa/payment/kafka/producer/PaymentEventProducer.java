package com.msa.payment.kafka.producer;

import com.msa.payment.config.KafkaConfig;
import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 결제 관련 Kafka 이벤트 발행자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    /** Kafka 메시지 발행 템플릿. */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 결제 완료 이벤트를 {@code payment.completed} 토픽으로 발행한다.
     *
     * @param event 발행할 결제 완료 이벤트
     */
    public void sendPaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 발행 - orderId: {}, eventId: {}", event.orderId(), event.eventId());
        kafkaTemplate
            .send(KafkaConfig.PAYMENT_COMPLETED_TOPIC, String.valueOf(event.orderId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("결제 완료 이벤트 발행 실패 - orderId: {}, error: {}", event.orderId(), ex.getMessage());
                } else {
                    log.info("결제 완료 이벤트 발행 성공 - orderId: {}, offset: {}", event.orderId(), result.getRecordMetadata().offset());
                }
            });
    }

    /**
     * 결제 실패 이벤트를 {@code payment.failed} 토픽으로 발행한다.
     *
     * @param event 발행할 결제 실패 이벤트
     */
    public void sendPaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 발행 - orderId: {}, reason: {}, eventId: {}", event.orderId(), event.reason(), event.eventId());
        kafkaTemplate
            .send(KafkaConfig.PAYMENT_FAILED_TOPIC, String.valueOf(event.orderId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("결제 실패 이벤트 발행 실패 - orderId: {}, error: {}", event.orderId(), ex.getMessage());
                } else {
                    log.info("결제 실패 이벤트 발행 성공 - orderId: {}, offset: {}", event.orderId(), result.getRecordMetadata().offset());
                }
            });
    }
}
