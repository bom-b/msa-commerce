package com.msa.payment.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 주문 생성 이벤트 DTO. Order Service가 {@code order.created} 토픽으로 발행하며,
 * Payment Service가 구독하여 결제 처리를 시작한다.
 *
 * @param eventId     이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId     생성된 주문 ID
 * @param userId      주문자 ID
 * @param productId   상품 ID
 * @param quantity    주문 수량
 * @param totalAmount 주문 총 금액
 */
public record OrderCreatedEvent(
    UUID eventId,
    Long orderId,
    Long userId,
    Long productId,
    int quantity,
    BigDecimal totalAmount) {
}
