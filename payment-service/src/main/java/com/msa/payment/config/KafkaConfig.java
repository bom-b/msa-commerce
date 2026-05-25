package com.msa.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 토픽 설정 클래스.
 */
@Configuration
public class KafkaConfig {

    /** order.created 토픽명 상수. */
    public static final String ORDER_CREATED_TOPIC = "order.created";

    /** payment.completed 토픽명 상수. */
    public static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";

    /** payment.failed 토픽명 상수. */
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    /** stock.insufficient 토픽명 상수. */
    public static final String STOCK_INSUFFICIENT_TOPIC = "stock.insufficient";

    /**
     * 결제 완료 이벤트를 발행할 Kafka 토픽 Bean.
     *
     * @return payment.completed 토픽 (파티션 1, 복제 팩터 1)
     */
    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(PAYMENT_COMPLETED_TOPIC).partitions(1).replicas(1).build();
    }

    /**
     * 결제 실패 이벤트를 발행할 Kafka 토픽 Bean.
     *
     * @return payment.failed 토픽 (파티션 1, 복제 팩터 1)
     */
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_TOPIC).partitions(1).replicas(1).build();
    }
}
