package com.msa.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UserBalance 도메인 단위 테스트.
 */
class UserBalanceTest {

    /**
     * 잔액이 정확히 1억에 도달하는 충전은 허용되는지 검증한다.
     */
    @Test
    @DisplayName("충전 후 잔액이 정확히 1억이면 충전에 성공한다")
    void chargeReachingMaxBalanceSucceeds() {
        UserBalance balance = UserBalance.builder().balance(90_000_000L).build();
        balance.charge(10_000_000L);
        assertThat(balance.getBalance()).isEqualTo(100_000_000L);
    }

    /**
     * 잔액이 1억을 초과하는 충전은 예외를 던지는지 검증한다.
     */
    @Test
    @DisplayName("충전 후 잔액이 1억을 초과하면 예외를 던진다")
    void chargeExceedingMaxBalanceThrows() {
        UserBalance balance = UserBalance.builder().balance(90_000_000L).build();
        assertThatThrownBy(() -> balance.charge(10_000_001L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상한");
    }

    /**
     * 상한을 초과하는 충전 시 잔액이 변경되지 않는지 검증한다.
     */
    @Test
    @DisplayName("상한 초과 충전 시 잔액 상태가 변경되지 않는다")
    void chargeExceedingMaxBalanceKeepsStateUnchanged() {
        UserBalance balance = UserBalance.builder().balance(90_000_000L).build();
        assertThatThrownBy(() -> balance.charge(20_000_000L)).isInstanceOf(IllegalArgumentException.class);
        assertThat(balance.getBalance()).isEqualTo(90_000_000L);
    }
}
