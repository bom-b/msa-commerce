package com.msa.stock.repository;

import com.msa.stock.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 재고 예약 JPA 레포지토리.
 */
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    /**
     * 주문 ID로 재고 예약을 조회한다.
     *
     * @param orderId 주문 ID
     * @return 재고 예약 Optional
     */
    Optional<StockReservation> findByOrderId(Long orderId);
}
