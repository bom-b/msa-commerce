package com.msa.order.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 결제 실패 이벤트 DTO. Payment Service가 {@code payment.failed} 토픽으로 발행하며,
 * Order Service가 구독하여 주문을 CANCELLED 상태로 전이시킨다.
 *
 * @param eventId   이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId   주문 ID
 * @param paymentId 결제 ID
 * @param amount    결제 시도 금액
 * @param reason    결제 실패 사유
 */
public record PaymentFailedEvent(
    UUID eventId, Long orderId, Long paymentId, BigDecimal amount, String reason) {
}
