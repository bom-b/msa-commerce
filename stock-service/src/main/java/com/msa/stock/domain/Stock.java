package com.msa.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 엔티티.
 */
@Entity
@Table(name = "stocks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    /** 재고 PK. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 재고와 1:1 연관된 상품. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    /** 현재 재고 수량. */
    @Column(nullable = false)
    private int quantity;

    /**
     * 재고를 차감한다.
     *
     * @param amount 차감할 수량
     * @throws IllegalArgumentException 현재 재고가 차감 수량보다 적을 때
     */
    public void decreaseQuantity(int amount) {
        if (this.quantity < amount) {
            throw new IllegalArgumentException("재고 부족: 현재 재고=" + this.quantity + ", 요청=" + amount);
        }
        this.quantity -= amount;
    }
}
