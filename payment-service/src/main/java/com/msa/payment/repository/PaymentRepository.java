package com.msa.payment.repository;

import com.msa.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 결제 데이터 접근 레포지토리.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문 ID와 주문자 ID로 결제를 조회한다.
     *
     * @param orderId 조회할 주문 ID
     * @param userId  조회할 주문자 ID
     * @return 결제 Optional 객체
     */
    Optional<Payment> findByOrderIdAndUserId(Long orderId, Long userId);

    /**
     * 주문 ID에 해당하는 결제 존재 여부를 반환한다.
     *
     * @param orderId 확인할 주문 ID
     * @return 결제 레코드 존재 여부
     */
    boolean existsByOrderId(Long orderId);
}
