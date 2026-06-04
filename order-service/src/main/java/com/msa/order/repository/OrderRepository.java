package com.msa.order.repository;

import com.msa.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

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

    /**
     * 주문 ID와 사용자 ID가 모두 일치하는 주문을 조회한다.
     *
     * @param id     조회할 주문 ID
     * @param userId 조회할 사용자 ID
     * @return 소유자가 일치하는 주문 (없으면 빈 Optional)
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
