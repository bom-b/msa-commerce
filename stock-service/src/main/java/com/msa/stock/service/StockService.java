package com.msa.stock.service;

import com.msa.stock.domain.Stock;
import com.msa.stock.dto.StockResponse;
import com.msa.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 재고 비즈니스 로직 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    /**
     * 재고 레포지토리.
     */
    private final StockRepository stockRepository;

    /**
     * 전체 재고 목록을 조회한다.
     *
     * @return 재고 응답 DTO 목록
     */
    public List<StockResponse> findAll() {
        return stockRepository.findAllWithProduct().stream()
            .map(StockResponse::from)
            .toList();
    }

    /**
     * 상품 ID로 재고를 조회한다.
     *
     * @param productId 상품 ID
     * @return 재고 응답 DTO
     */
    public StockResponse findByProductId(Long productId) {
        Stock stock = stockRepository.findByProduct_Id(productId)
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
        return StockResponse.from(stock);
    }
}
