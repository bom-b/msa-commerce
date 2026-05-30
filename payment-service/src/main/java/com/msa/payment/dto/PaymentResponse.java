package com.msa.payment.dto;

import com.msa.payment.domain.Payment;
import com.msa.payment.domain.PaymentStatus;

import java.time.LocalDateTime;

/**
 * 결제 응답 DTO.
 *
 * @param id        결제 ID
 * @param orderId   주문 ID
 * @param amount    결제 금액
 * @param status    결제 상태
 * @param createdAt 결제 생성 일시
 */
public record PaymentResponse(Long id, Long orderId, Long amount, PaymentStatus status, LocalDateTime createdAt) {

    /**
     * Payment 엔티티로부터 PaymentResponse를 생성하는 팩토리 메서드.
     *
     * @param payment Payment 엔티티
     * @return PaymentResponse 인스턴스
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getCreatedAt());
    }
}
