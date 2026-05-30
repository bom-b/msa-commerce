package com.msa.common.event;

import java.util.UUID;

/**
 * 주문 생성 이벤트 DTO.
 *
 * @param eventId   이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId   생성된 주문 ID
 * @param userId    주문자 ID
 * @param productId 상품 ID
 * @param quantity  주문 수량
 */
public record OrderCreatedEvent(
    UUID eventId,
    Long orderId,
    Long userId,
    Long productId,
    int quantity
) {
}
