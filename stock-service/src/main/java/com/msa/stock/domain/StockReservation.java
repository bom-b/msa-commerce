package com.msa.stock.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 재고 예약 엔티티.
 */
@Entity
@Table(name = "stock_reservations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    /**
     * 재고 예약 PK.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문 ID.
     */
    @Column(nullable = false, unique = true)
    private Long orderId;

    /**
     * 재고 ID.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * 예약한 수량
     */
    @Column(nullable = false)
    private int reservedQuantity;

    /**
     * 예약 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private StockReservation(Long orderId, Stock stock, int reservedQuantity, ReservationStatus status) {
        this.orderId = orderId;
        this.stock = stock;
        this.reservedQuantity = reservedQuantity;
        this.status = status;
    }

    /**
     * 재고 예약을 생성하고 Stock의 가용 재고를 차감한다.
     *
     * @param stock   예약할 재고 엔티티
     * @param orderId 주문 ID
     * @param quantity 예약 수량
     * @return 생성된 재고 예약 엔티티
     * @throws IllegalArgumentException 가용 재고가 부족할 경우
     */
    public static StockReservation createReservation(Stock stock, Long orderId, int quantity) {
        stock.reserveQuantity(quantity);

        return StockReservation.builder()
            .orderId(orderId)
            .stock(stock)
            .reservedQuantity(quantity)
            .status(ReservationStatus.PENDING)
            .build();
    }

    /**
     * 예약을 확정하고 물리 재고를 차감한다.
     */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.stock.confirmQuantity(this.reservedQuantity);
    }

    /**
     * 예약을 취소하고 가용 재고를 복구한다.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.stock.restoreQuantity(this.reservedQuantity);
    }
}
