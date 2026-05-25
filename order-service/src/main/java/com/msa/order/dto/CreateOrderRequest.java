package com.msa.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 주문 생성 요청 DTO.
 *
 * @param productId   상품 ID (필수)
 * @param quantity    주문 수량 (최소 1)
 * @param totalAmount 주문 총 금액 (0 초과)
 */
public record CreateOrderRequest(
    @NotNull(message = "상품 ID는 필수입니다.") Long productId,
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.") int quantity,
    @NotNull(message = "총 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "총 금액은 0보다 커야 합니다.")
    BigDecimal totalAmount) {
}
