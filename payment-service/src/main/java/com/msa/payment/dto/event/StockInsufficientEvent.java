package com.msa.payment.dto.event;

import java.util.UUID;

/**
 * 재고 부족 이벤트 DTO. Stock Service가 {@code stock.insufficient} 토픽으로 발행하며,
 * Payment Service가 구독하여 결제를 REFUNDED 상태로 전이하고 {@code payment.failed}를 발행한다.
 *
 * @param eventId   이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId   주문 ID
 * @param productId 상품 ID
 * @param quantity  요청된 수량
 * @param reason    재고 부족 사유
 */
public record StockInsufficientEvent(
    UUID eventId, Long orderId, Long productId, int quantity, String reason) {
}
