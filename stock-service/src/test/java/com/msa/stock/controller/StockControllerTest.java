package com.msa.stock.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.msa.stock.dto.StockResponse;
import com.msa.stock.service.StockService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * StockController 통합 테스트.
 * WebMvcTest 슬라이스로 StockService를 목킹하여 HTTP 레이어를 검증한다.
 */
@WebMvcTest(StockController.class)
class StockControllerTest {

    /** MockMvc HTTP 요청 수행 객체. */
    @Autowired
    private MockMvc mockMvc;

    /** 목킹된 재고 서비스. */
    @MockBean
    private StockService stockService;

    /**
     * GET /stocks 요청 시 200 OK와 전체 재고 목록 JSON을 반환하는지 검증한다.
     *
     * @throws Exception MockMvc 수행 중 발생하는 예외
     */
    @Test
    @DisplayName("GET /stocks - 전체 재고 목록 반환")
    void getStocks_returns200WithStockList() throws Exception {
        // given
        List<StockResponse> stocks = List.of(
                new StockResponse(1L, 1L, "노트북", 100),
                new StockResponse(2L, 2L, "마우스", 100),
                new StockResponse(3L, 3L, "키보드", 100),
                new StockResponse(4L, 4L, "모니터", 100),
                new StockResponse(5L, 5L, "헤드셋", 100)
        );
        given(stockService.findAll()).willReturn(stocks);

        // when & then
        mockMvc.perform(get("/stocks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("노트북"))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[4].productName").value("헤드셋"));
    }

    /**
     * GET /stocks 요청 시 재고가 없는 경우 200 OK와 빈 배열을 반환하는지 검증한다.
     *
     * @throws Exception MockMvc 수행 중 발생하는 예외
     */
    @Test
    @DisplayName("GET /stocks - 재고가 없을 때 빈 배열 반환")
    void getStocks_returns200WithEmptyList_whenNoStocks() throws Exception {
        // given
        given(stockService.findAll()).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/stocks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
