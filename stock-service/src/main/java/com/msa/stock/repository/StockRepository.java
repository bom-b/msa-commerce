package com.msa.stock.repository;

import com.msa.stock.domain.Stock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 재고 JPA 리포지토리.
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 상품 ID로 재고를 조회한다.
     *
     * @param productId 상품 ID
     * @return 재고 Optional
     */
    Optional<Stock> findByProduct_Id(Long productId);

    /**
     * 전체 재고를 상품 정보와 함께 조회한다.
     *
     * @return Product가 로딩된 Stock 목록
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.product")
    List<Stock> findAllWithProduct();
}
