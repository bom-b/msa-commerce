package com.msa.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 생성 요청 DTO.
 *
 * @param productId 상품 ID (필수)
 * @param quantity  주문 수량 (최소 1)
 */
public record CreateOrderRequest(
    @NotNull(message = "상품 ID는 필수입니다.") Long productId,
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.") int quantity) {
}
