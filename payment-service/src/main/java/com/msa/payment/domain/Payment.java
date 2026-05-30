package com.msa.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 JPA 엔티티.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    /** 결제 고유 식별자 (자동 생성). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 결제에 해당하는 주문 ID. */
    @Column(nullable = false, unique = true)
    private Long orderId;

    /** 결제 금액 (원화). */
    @Column(nullable = false)
    private Long amount;

    /** 결제 상태 (COMPLETED, FAILED, REFUNDED). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** 결제 생성 일시. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 결제 엔티티 생성자 (빌더 전용).
     *
     * @param orderId   주문 ID
     * @param amount    결제 금액
     * @param status    결제 상태
     * @param createdAt 생성 일시
     */
    @Builder
    private Payment(Long orderId, Long amount, PaymentStatus status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * 결제 상태를 변경한다.
     *
     * @param status 변경할 결제 상태
     */
    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}
