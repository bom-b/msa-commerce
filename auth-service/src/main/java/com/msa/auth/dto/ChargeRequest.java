package com.msa.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 예치금 충전 요청 DTO.
 *
 * @param amount 충전할 금액 (원화, 양수)
 */
public record ChargeRequest(
        @NotNull(message = "충전 금액은 필수입니다.")
        @Positive(message = "충전 금액은 0보다 커야 합니다.")
        BigDecimal amount
) {
}
