package com.msa.stock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Stock 도메인 단위 테스트.
 */
class StockTest {

    /**
     * 입고 후 총 재고가 정확히 999에 도달하는 입고는 허용되는지 검증한다.
     */
    @Test
    @DisplayName("입고 후 총 재고가 정확히 999이면 입고에 성공한다")
    void addQuantityReachingMaxSucceeds() {
        Stock stock = Stock.builder().totalQuantity(900).availableQuantity(900).build();
        stock.addQuantity(99);
        assertThat(stock.getTotalQuantity()).isEqualTo(999);
        assertThat(stock.getAvailableQuantity()).isEqualTo(999);
    }

    /**
     * 입고 후 총 재고가 999를 초과하는 입고는 예외를 던지는지 검증한다.
     */
    @Test
    @DisplayName("입고 후 총 재고가 999를 초과하면 예외를 던진다")
    void addQuantityExceedingMaxThrows() {
        Stock stock = Stock.builder().totalQuantity(900).availableQuantity(900).build();
        assertThatThrownBy(() -> stock.addQuantity(100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초과할 수 없습니다");
    }

    /**
     * 상한을 초과하는 입고 시 총 재고와 가용 재고가 변경되지 않는지 검증한다.
     */
    @Test
    @DisplayName("상한 초과 입고 시 재고 상태가 변경되지 않는다")
    void addQuantityExceedingMaxKeepsStateUnchanged() {
        Stock stock = Stock.builder().totalQuantity(900).availableQuantity(900).build();
        assertThatThrownBy(() -> stock.addQuantity(200)).isInstanceOf(IllegalArgumentException.class);
        assertThat(stock.getTotalQuantity()).isEqualTo(900);
        assertThat(stock.getAvailableQuantity()).isEqualTo(900);
    }
}
