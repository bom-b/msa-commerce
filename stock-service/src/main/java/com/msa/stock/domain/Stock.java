package com.msa.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 엔티티.
 * 상품별 재고 수량을 관리한다.
 */
@Entity
@Table(name = "stocks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    /** 재고 ID (기본키, 자동 증가). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품 ID. */
    @Column(nullable = false, unique = true)
    private Long productId;

    /** 상품명. */
    @Column(nullable = false)
    private String productName;

    /** 현재 재고 수량. */
    @Column(nullable = false)
    private int quantity;

    /**
     * 재고 수량을 차감한다.
     *
     * @param amount 차감할 수량
     * @throws IllegalArgumentException 차감 후 재고가 음수가 되는 경우
     */
    public void decreaseQuantity(int amount) {
        if (this.quantity < amount) {
            throw new IllegalArgumentException("재고 부족: 현재 재고=" + this.quantity + ", 요청=" + amount);
        }
        this.quantity -= amount;
    }
}
