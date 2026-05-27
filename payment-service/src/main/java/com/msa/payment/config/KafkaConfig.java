package com.msa.payment.config;

import com.msa.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 토픽 설정 클래스.
 */
@Configuration
public class KafkaConfig {

    /**
     * 결제 완료 이벤트를 발행할 Kafka 토픽 Bean.
     *
     * @return payment.completed 토픽 (파티션 1, 복제 팩터 1)
     */
    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMPLETED).partitions(1).replicas(1).build();
    }

    /**
     * 결제 실패 이벤트를 발행할 Kafka 토픽 Bean.
     *
     * @return payment.failed 토픽 (파티션 1, 복제 팩터 1)
     */
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED).partitions(1).replicas(1).build();
    }
}
