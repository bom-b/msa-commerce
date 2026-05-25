package com.msa.stock.service;

import com.msa.stock.dto.StockResponse;
import com.msa.stock.repository.StockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 재고 비즈니스 로직 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    /** 재고 레포지토리. */
    private final StockRepository stockRepository;

    /**
     * 전체 재고 목록을 조회한다.
     *
     * @return 재고 응답 DTO 목록
     */
    public List<StockResponse> findAll() {
        return stockRepository.findAll().stream()
                .map(StockResponse::from)
                .toList();
    }
}
