package com.msa.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 재고 입고 요청 DTO.
 *
 * @param quantity 입고할 수량 (1 이상)
 */
public record StockAddRequest(
        @NotNull(message = "입고 수량은 필수입니다.")
        @Min(value = 1, message = "입고 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
