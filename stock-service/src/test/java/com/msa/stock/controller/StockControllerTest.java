package com.msa.stock.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.stock.dto.StockAddRequest;
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
 */
@WebMvcTest(StockController.class)
class StockControllerTest {

    /** MockMvc HTTP 요청 수행 객체. */
    @Autowired
    private MockMvc mockMvc;

    /** JSON 직렬화 객체. */
    @Autowired
    private ObjectMapper objectMapper;

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
                new StockResponse(1L, 1L, "노트북", 100, "notebook.webp", 1200000L),
                new StockResponse(2L, 2L, "마우스", 100, "mouse.webp", 35000L),
                new StockResponse(3L, 3L, "키보드", 100, "keyboard.webp", 89000L),
                new StockResponse(4L, 4L, "모니터", 100, "monitor.webp", 450000L),
                new StockResponse(5L, 5L, "헤드셋", 100, "headset.webp", 120000L)
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

    /**
     * POST /stocks/{productId}/quantity 요청 시 200 OK와 갱신된 재고 JSON을 반환하는지 검증한다.
     *
     * @throws Exception MockMvc 수행 중 발생하는 예외
     */
    @Test
    @DisplayName("POST /stocks/{productId}/quantity - 입고 성공 시 갱신된 재고 반환")
    void addStock_returns200WithUpdatedStock() throws Exception {
        // given
        StockResponse updated = new StockResponse(1L, 1L, "노트북", 120, "notebook.webp", 1200000L);
        given(stockService.addStock(eq(1L), eq(20))).willReturn(updated);

        // when & then
        mockMvc.perform(post("/stocks/1/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StockAddRequest(20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(120));
        verify(stockService).addStock(1L, 20);
    }

    /**
     * POST /stocks/{productId}/quantity 요청 시 quantity가 1 미만이면 400을 반환하고 서비스를 호출하지 않는지 검증한다.
     *
     * @throws Exception MockMvc 수행 중 발생하는 예외
     */
    @Test
    @DisplayName("POST /stocks/{productId}/quantity - quantity가 0이면 400 반환")
    void addStock_returns400_whenQuantityBelowMin() throws Exception {
        // when & then
        mockMvc.perform(post("/stocks/1/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StockAddRequest(0))))
                .andExpect(status().isBadRequest());
        verify(stockService, never()).addStock(eq(1L), anyInt());
    }
}
