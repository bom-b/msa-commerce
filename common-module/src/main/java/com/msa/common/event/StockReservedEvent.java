package com.msa.common.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 재고 확보 완료 이벤트 DTO. Stock Service가 {@code stock.reserved} 토픽으로 발행하며,
 * Payment Service가 구독하여 결제를 처리한다.
 *
 * @param eventId     이벤트 고유 식별자 (멱등성 처리용 UUID)
 * @param orderId     주문 ID
 * @param userId      주문자 ID
 * @param productId   상품 ID
 * @param quantity    확보된 재고 수량
 * @param totalAmount 결제할 주문 총 금액
 */
public record StockReservedEvent(
    UUID eventId,
    Long orderId,
    Long userId,
    Long productId,
    int quantity,
    BigDecimal totalAmount
) {
}
