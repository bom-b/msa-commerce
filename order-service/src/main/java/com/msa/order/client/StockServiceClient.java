package com.msa.order.client;

import com.msa.order.dto.StockResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.NoSuchElementException;

/**
 * 재고 서비스 클라이언트.
 */
@Component
public class StockServiceClient {

    private final RestClient restClient;

    public StockServiceClient(@Qualifier("stockRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * 상품 ID로 재고 정보를 조회한다.
     *
     * @param productId 상품 ID
     * @return 재고 응답 DTO
     * @throws NoSuchElementException 재고 정보를 조회할 수 없는 경우
     */
    public StockResponse getStock(Long productId) {
        try {
            return restClient.get()
                .uri("/stocks/{productId}", productId)
                .retrieve()
                .body(StockResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("재고 정보를 조회할 수 없습니다.");
        }
    }
}
