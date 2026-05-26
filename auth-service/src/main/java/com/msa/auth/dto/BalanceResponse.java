package com.msa.auth.dto;

import java.math.BigDecimal;

/**
 * 예치금 조회 응답 DTO.
 *
 * @param userId  사용자 ID
 * @param balance 현재 예치금 잔액 (원화)
 */
public record BalanceResponse(Long userId, BigDecimal balance) {}
