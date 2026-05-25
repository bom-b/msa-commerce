package com.msa.stock.dto;

import com.msa.stock.domain.Stock;

/**
 * 재고 조회 응답 DTO.
 *
 * @param id          재고 ID
 * @param productId   상품 ID
 * @param productName 상품명
 * @param quantity    현재 재고 수량
 */
public record StockResponse(
        Long id,
        Long productId,
        String productName,
        int quantity
) {

    /**
     * Stock 엔티티로부터 StockResponse를 생성한다.
     *
     * @param stock Stock 엔티티
     * @return StockResponse
     */
    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getProductId(),
                stock.getProductName(),
                stock.getQuantity()
        );
    }
}
