package com.msa.order.repository;

import com.msa.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 주문 데이터 접근 레포지토리.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 사용자 ID로 주문 목록을 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 주문 목록
     */
    List<Order> findAllByUserId(Long userId);
}
