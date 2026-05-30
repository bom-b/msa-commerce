package com.msa.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 예치금 차감 요청 DTO.
 *
 * @param userId 차감할 사용자 ID
 * @param amount 차감 금액 (원화, 양수)
 */
public record DeductRequest(
    @NotNull(message = "사용자 ID는 필수입니다.") Long userId,
    @NotNull(message = "차감 금액은 필수입니다.") @Positive(message = "차감 금액은 0보다 커야 합니다.") Long amount) {
}
