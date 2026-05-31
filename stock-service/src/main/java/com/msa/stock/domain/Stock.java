package com.msa.stock.domain;

import jakarta.persistence.*;
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

    /**
     * 총 재고 수량 상한(999개).
     */
    private static final int MAX_QUANTITY = 999;

    /**
     * 재고 PK.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 재고와 1:1 연관된 상품.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    /**
     * 물리적인 총 재고
     */
    @Column(nullable = false)
    private int totalQuantity;

    /**
     * 현재 주문할 수 있는 가용 재고
     */
    @Column(nullable = false)
    private int availableQuantity;

    /**
     * 재고를 예약한다.
     *
     * @param amount 예약할 수량
     * @throws IllegalArgumentException 현재 재고가 차감 수량보다 적을 때
     */
    public void reserveQuantity(int amount) {
        if (this.availableQuantity < amount) {
            throw new IllegalArgumentException("재고 부족: 현재 가용 재고=" + this.availableQuantity + ", 요청=" + amount);
        }
        this.availableQuantity -= amount;
    }

    /**
     * 결제 완료 시 물리적 총 재고를 차감한다.
     *
     * @param amount 차감할 수량
     */
    public void confirmQuantity(int amount) {
        this.totalQuantity -= amount;
    }

    /**
     * 예약 취소 시 가용 재고를 복구한다.
     *
     * @param amount 복구할 수량
     */
    public void restoreQuantity(int amount) {
        this.availableQuantity += amount;
    }

    /**
     * 신규 입고분을 총 재고와 가용 재고에 함께 더한다.
     *
     * @param amount 입고할 수량
     * @throws IllegalArgumentException 입고 수량이 1 미만일 때, 또는 입고 후 총 재고가 999를 초과할 때
     */
    public void addQuantity(int amount) {
        if (this.totalQuantity + amount > MAX_QUANTITY) {
            throw new IllegalArgumentException("재고는 " + MAX_QUANTITY + "개를 초과할 수 없습니다.");
        }

        this.totalQuantity += amount;
        this.availableQuantity += amount;
    }
}
