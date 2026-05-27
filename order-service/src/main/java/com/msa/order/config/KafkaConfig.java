package com.msa.order.config;

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
     * 주문 생성 이벤트를 발행할 Kafka 토픽 Bean.
     *
     * @return order.created 토픽 (파티션 1, 복제 팩터 1)
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED).partitions(1).replicas(1).build();
    }
}
