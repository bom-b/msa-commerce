package com.msa.stock.repository;

import com.msa.stock.domain.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 재고 JPA 레포지토리.
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 상품 ID로 재고를 조회한다.
     *
     * @param productId 상품 ID
     * @return 재고 Optional
     */
    Optional<Stock> findByProductId(Long productId);
}
