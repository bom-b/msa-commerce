package com.msa.order.repository;

import com.msa.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 데이터 접근 레포지토리.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
