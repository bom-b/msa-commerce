package com.msa.payment.domain;

/**
 * 결제 상태 열거형.
 */
public enum PaymentStatus {

    /** 예치금 차감 진행 중. */
    PENDING,

    /** 결제 완료. */
    COMPLETED,

    /** 결제 실패 (잔액 부족 등). */
    FAILED,

    /** 결제 환불. */
    REFUNDED
}
