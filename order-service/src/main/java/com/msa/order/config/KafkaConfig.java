package com.msa.order.config;

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
     * order.created 토픽명 상수.
     */
    public static final String ORDER_CREATED_TOPIC = "order.created";

    /**
     * payment.completed 토픽명 상수.
     */
    public static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";

    /**
     * payment.failed 토픽명 상수.
     */
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    /**
     * stock.reserved 토픽명 상수.
     */
    public static final String STOCK_RESERVED_TOPIC = "stock.reserved";

    /**
     * stock.insufficient 토픽명 상수.
     */
    public static final String STOCK_INSUFFICIENT_TOPIC = "stock.insufficient";

    /**
     * 주문 생성 이벤트를 발행할 Kafka 토픽 Bean.
     *
     * @return order.created 토픽 (파티션 1, 복제 팩터 1)
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC).partitions(1).replicas(1).build();
    }
}
