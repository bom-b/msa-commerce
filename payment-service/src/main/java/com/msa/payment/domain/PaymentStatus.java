package com.msa.payment.domain;

/**
 * 결제 상태 열거형.
 */
public enum PaymentStatus {

    /** 결제 완료. */
    COMPLETED,

    /** 결제 실패. */
    FAILED,

    /** 결제 환불. */
    REFUNDED
}
