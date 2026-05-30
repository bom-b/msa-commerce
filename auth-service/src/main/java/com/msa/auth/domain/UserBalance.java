package com.msa.auth.domain;

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
 * 사용자 예치금 엔티티. user_balances 테이블과 매핑된다.
 */
@Entity
@Table(name = "user_balances")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {

    /** 예치금 레코드 ID (기본키, 자동 증가). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 예치금 소유 사용자. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** 현재 예치금 잔액 (원화). */
    @Column(nullable = false)
    private Long balance;

    /**
     * 예치금을 차감한다.
     *
     * @param amount 차감할 금액
     * @throws IllegalArgumentException 잔액이 차감 금액보다 부족한 경우
     */
    public void deduct(Long amount) {
        if (this.balance < amount) {
            throw new IllegalArgumentException("잔액 부족: 현재 잔액=" + this.balance + ", 요청=" + amount);
        }
        this.balance = this.balance - amount;
    }

    /**
     * 예치금을 충전한다.
     *
     * @param amount 충전할 금액
     * @throws IllegalArgumentException 충전 금액이 null이거나 0 이하인 경우
     */
    public void charge(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance + amount;
    }
}
