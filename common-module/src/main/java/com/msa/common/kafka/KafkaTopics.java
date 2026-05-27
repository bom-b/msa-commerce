package com.msa.common.kafka;

/**
 * 전 서비스 공통 Kafka 토픽명 상수.
 */
public final class KafkaTopics {

    /** 주문 생성 토픽. */
    public static final String ORDER_CREATED = "order.created";

    /** 결제 완료 토픽. */
    public static final String PAYMENT_COMPLETED = "payment.completed";

    /** 결제 실패 토픽. */
    public static final String PAYMENT_FAILED = "payment.failed";

    /** 재고 확보 완료 토픽. */
    public static final String STOCK_RESERVED = "stock.reserved";

    /** 재고 부족 토픽. */
    public static final String STOCK_INSUFFICIENT = "stock.insufficient";

    /** 유틸리티 클래스 — 인스턴스 생성 불가. */
    private KafkaTopics() {}
}
