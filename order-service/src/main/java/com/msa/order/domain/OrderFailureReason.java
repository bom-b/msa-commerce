package com.msa.order.domain;

/**
 * 주문 실패 사유 열거형.
 */
public enum OrderFailureReason {

    /** 재고 부족으로 인한 주문 취소. */
    STOCK_INSUFFICIENT("재고가 부족합니다."),

    /** 잔액 부족으로 인한 주문 취소. */
    INSUFFICIENT_BALANCE("잔액이 부족합니다.");

    /** 클라이언트에 반환할 한글 메시지. */
    private final String message;

    /**
     * @param message 클라이언트에 반환할 한글 메시지
     */
    OrderFailureReason(String message) {
        this.message = message;
    }

    /**
     * 클라이언트에 반환할 한글 메시지를 반환한다.
     *
     * @return 한글 실패 메시지
     */
    public String getMessage() {
        return message;
    }
}
