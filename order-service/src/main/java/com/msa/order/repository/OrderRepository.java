package com.msa.order.repository;

import com.msa.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 데이터 접근 레포지토리.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 사용자 ID로 주문 목록을 페이지네이션하여 조회한다.
     *
     * @param userId   조회할 사용자 ID
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이지네이션된 주문 목록
     */
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}
