package com.msa.stock.dto;

import com.msa.stock.domain.Stock;

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
        Long price
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
                stock.getProduct().getId(),
                stock.getProduct().getProductName(),
                stock.getAvailableQuantity(),
                stock.getProduct().getImageName(),
                stock.getProduct().getPrice()
        );
    }
}
