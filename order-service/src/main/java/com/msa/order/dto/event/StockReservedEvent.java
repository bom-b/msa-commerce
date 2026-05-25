package com.msa.order.dto.event;

import java.util.UUID;

/**
 * 재고 확보 완료 이벤트 DTO. Stock Service가 {@code stock.reserved} 토픽으로 발행하며,
 * Order Service가 구독하여 주문을 COMPLETED 상태로 전이시킨다.
 *
 * @param eventId   이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId   주문 ID
 * @param productId 상품 ID
 * @param quantity  확보된 재고 수량
 */
public record StockReservedEvent(UUID eventId, Long orderId, Long productId, int quantity) {
}
