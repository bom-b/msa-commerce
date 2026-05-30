package com.msa.common.event;

import java.util.UUID;

/**
 * 재고 부족 이벤트 DTO. Stock Service가 {@code stock.insufficient} 토픽으로 발행하며,
 * Order Service가 구독하여 주문을 CANCELLED 상태로 전이시키고,
 * Payment Service가 구독하여 결제를 환불 처리한다.
 *
 * @param eventId   이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId   주문 ID
 * @param productId 상품 ID
 * @param quantity  요청된 수량
 * @param reason    재고 부족 사유
 */
public record StockInsufficientEvent(
    UUID eventId,
    Long orderId,
    Long productId,
    int quantity,
    String reason
) {
}
