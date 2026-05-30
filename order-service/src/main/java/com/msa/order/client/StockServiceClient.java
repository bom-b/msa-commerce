package com.msa.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

/**
 * Stock Service REST API 호출 클라이언트.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockServiceClient {

    private final RestClient stockRestClient;

    /**
     * Stock Service에서 단일 상품의 재고 정보를 조회한다.
     *
     * @param productId 조회할 상품 ID
     * @return 상품명과 가격이 포함된 StockInfo 레코드
     * @throws NoSuchElementException 상품이 존재하지 않거나 호출에 실패한 경우
     */
    public StockInfo getStock(Long productId) {
        try {
            StockInfo stockInfo = stockRestClient.get()
                .uri("/stocks/{productId}", productId)
                .retrieve()
                .body(StockInfo.class);

            if (stockInfo == null) {
                throw new NoSuchElementException("상품을 찾을 수 없습니다. productId: " + productId);
            }

            log.debug("상품 정보 조회 완료 - productId: {}, productName: {}", productId, stockInfo.productName());
            return stockInfo;
        } catch (RestClientException e) {
            log.error("Stock Service 호출 실패 - productId: {}, error: {}", productId, e.getMessage());
            throw new NoSuchElementException("상품을 찾을 수 없습니다. productId: " + productId);
        }
    }

    /**
     * Stock Service 재고 조회 응답 중 주문에 필요한 상품 정보 DTO.
     *
     * @param productId   상품 ID
     * @param productName 상품명
     * @param price       상품 단가
     */
    public record StockInfo(
        Long productId,
        String productName,
        BigDecimal price
    ) {
    }
}
