package com.msa.order.domain;

/**
 * 주문 상태 열거형. PENDING → COMPLETED (stock.reserved) 또는 CANCELLED (payment.failed/stock.insufficient).
 */
public enum OrderStatus {

    /**
     * 결제 및 재고 처리 대기 중인 상태.
     */
    PENDING,

    /**
     * 결제 완료 및 재고 확보가 완료된 상태.
     */
    COMPLETED,

    /**
     * 결제 실패 또는 재고 부족으로 취소된 상태.
     */
    CANCELLED
}
