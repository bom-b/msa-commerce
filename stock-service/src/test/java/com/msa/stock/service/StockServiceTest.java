package com.msa.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.msa.stock.domain.Product;
import com.msa.stock.domain.Stock;
import com.msa.stock.dto.StockResponse;
import com.msa.stock.repository.StockRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * StockService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    /** 테스트 대상 서비스. */
    @InjectMocks
    private StockService stockService;

    /** 목킹된 재고 리포지토리. */
    @Mock
    private StockRepository stockRepository;

    /**
     * findAll() 호출 시 전체 재고가 DTO로 변환되어 반환되는지 검증한다.
     */
    @Test
    @DisplayName("전체 재고 목록 조회 - 성공")
    void findAll_returnsAllStocks() {
        // given
        Product p1 = Product.builder().id(1L).productName("노트북").price(BigDecimal.valueOf(1200000)).build();
        Product p2 = Product.builder().id(2L).productName("마우스").price(BigDecimal.valueOf(35000)).build();
        Product p3 = Product.builder().id(3L).productName("키보드").price(BigDecimal.valueOf(89000)).build();
        List<Stock> stocks = List.of(
                Stock.builder().id(1L).product(p1).quantity(100).build(),
                Stock.builder().id(2L).product(p2).quantity(100).build(),
                Stock.builder().id(3L).product(p3).quantity(100).build()
        );
        given(stockRepository.findAllWithProduct()).willReturn(stocks);

        // when
        List<StockResponse> result = stockService.findAll();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).productName()).isEqualTo("노트북");
        assertThat(result.get(0).quantity()).isEqualTo(100);
        assertThat(result.get(1).productName()).isEqualTo("마우스");
        assertThat(result.get(2).productName()).isEqualTo("키보드");
        verify(stockRepository).findAllWithProduct();
    }

    /**
     * findAll() 호출 시 재고가 없는 경우 빈 목록을 반환하는지 검증한다.
     */
    @Test
    @DisplayName("전체 재고 목록 조회 - 빈 목록")
    void findAll_returnsEmptyList_whenNoStocks() {
        // given
        given(stockRepository.findAll()).willReturn(List.of());

        // when
        List<StockResponse> result = stockService.findAll();

        // then
        assertThat(result).isEmpty();
        verify(stockRepository).findAllWithProduct();
    }

    /**
     * StockResponse.from()이 Stock 및 Product 필드를 올바르게 매핑하는지 검증한다.
     */
    @Test
    @DisplayName("StockResponse DTO 변환 - 필드 매핑 검증")
    void findAll_mapsFieldsCorrectly() {
        // given
        Product product = Product.builder().id(5L).productName("헤드셋").imageName("headset.webp").price(BigDecimal.valueOf(120000)).build();
        Stock stock = Stock.builder().id(10L).product(product).quantity(50).build();
        given(stockRepository.findAll()).willReturn(List.of(stock));

        // when
        List<StockResponse> result = stockService.findAll();

        // then
        StockResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.productId()).isEqualTo(5L);
        assertThat(response.productName()).isEqualTo("헤드셋");
        assertThat(response.imageName()).isEqualTo("headset.webp");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(120000));
        assertThat(response.quantity()).isEqualTo(50);
    }
}
