package com.msa.order.dto;

import com.msa.order.domain.Order;
import com.msa.order.domain.OrderStatus;

import java.time.LocalDateTime;

/**
 * 주문 응답 DTO.
 *
 * @param id            주문 ID
 * @param userId        주문자 ID
 * @param productId     상품 ID
 * @param productName   상품명 스냅샷
 * @param quantity      주문 수량
 * @param totalAmount   주문 총 금액 스냅샷
 * @param status        주문 상태
 * @param createdAt     주문 생성 일시
 * @param failureReason 주문 실패 사유 (CANCELLED 상태일 때 설정)
 */
public record OrderResponse(
    Long id,
    Long userId,
    Long productId,
    String productName,
    int quantity,
    Long totalAmount,
    OrderStatus status,
    LocalDateTime createdAt,
    String failureReason
) {

    /**
     * Order 엔티티로부터 OrderResponse를 생성하는 팩토리 메서드.
     *
     * @param order Order 엔티티
     * @return OrderResponse 인스턴스
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getUserId(),
            order.getProductId(),
            order.getProductName(),
            order.getQuantity(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getFailureReason());
    }
}
