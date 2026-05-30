package com.msa.payment.kafka.producer;

import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.kafka.KafkaTopics;
import com.msa.common.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * 결제 관련 Kafka 이벤트 발행자.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 결제 완료 이벤트를 {@code payment.completed} 토픽으로 발행한다.
     *
     * @param event 발행할 결제 완료 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendPaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 발행 - orderId: {}", event.orderId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, String.valueOf(event.orderId()), event);
    }

    /**
     * 결제 실패 이벤트를 {@code payment.failed} 토픽으로 발행한다.
     *
     * @param event 발행할 결제 실패 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendPaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 발행 - orderId: {}, reason: {}", event.orderId(), event.reason());
        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, String.valueOf(event.orderId()), event);
    }
}
