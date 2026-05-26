package com.msa.order.dto;

import java.math.BigDecimal;

/**
 * 재고 조회 응답 DTO.
 *
 * @param id          재고 ID
 * @param productId   상품 ID
 * @param productName 상품명
 * @param quantity    현재 재고 수량
 * @param imageName   상품 이미지 파일명
 * @param price       상품 가격 (원화)
 */
public record StockResponse(
    Long id,
    Long productId,
    String productName,
    int quantity,
    String imageName,
    BigDecimal price
) {
}
