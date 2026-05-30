package com.msa.stock.domain;

public enum ReservationStatus {
    /**
     * 결제 대기 중 (가용 재고만 차감된 상태)
     */
    PENDING,

    /**
     * 결제 완료 (물리 재고 차감 완료)
     */
    CONFIRMED,

    /**
     * 결제 실패/취소 (가용 재고 복구 완료)
     */
    CANCELLED
}
