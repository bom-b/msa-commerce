package com.msa.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 JPA 엔티티. 주문 생성 시 PENDING 상태로 저장되며, Kafka 이벤트 수신에 따라 상태가 전이된다.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    /**
     * 주문 고유 식별자 (자동 생성).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문을 생성한 사용자 ID.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 주문한 상품 ID.
     */
    @Column(nullable = false)
    private Long productId;

    /**
     * 주문 수량.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * 주문 상태 (PENDING, COMPLETED, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /**
     * 주문 총 금액.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 주문 생성 일시.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 주문 엔티티 생성자 (빌더 전용).
     *
     * @param userId      주문자 ID
     * @param productId   상품 ID
     * @param quantity    주문 수량
     * @param status      주문 상태
     * @param totalAmount 총 금액
     * @param createdAt   생성 일시
     */
    @Builder
    private Order(
        Long userId,
        Long productId,
        int quantity,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    /**
     * 주문 상태를 변경한다.
     *
     * @param status 변경할 주문 상태
     */
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
