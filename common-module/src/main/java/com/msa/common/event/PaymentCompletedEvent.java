package com.msa.common.event;

import java.util.UUID;

/**
 * 결제 완료 이벤트 DTO. Payment Service가 {@code payment.completed} 토픽으로 발행하며,
 * Stock Service가 구독하여 재고 차감을 처리한다.
 *
 * @param eventId   이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId   주문 ID
 * @param paymentId 결제 ID
 * @param amount    결제 금액
 */
public record PaymentCompletedEvent(
    UUID eventId,
    Long orderId,
    Long paymentId,
    Long amount
) {
}
