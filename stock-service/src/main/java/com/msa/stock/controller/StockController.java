package com.msa.stock.controller;

import com.msa.stock.dto.StockAddRequest;
import com.msa.stock.dto.StockResponse;
import com.msa.stock.service.StockService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 재고 REST 컨트롤러.
 */
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    /** 재고 서비스. */
    private final StockService stockService;

    /**
     * 전체 재고 목록을 반환한다.
     *
     * @return 재고 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<StockResponse>> getStocks() {
        return ResponseEntity.ok(stockService.findAll());
    }

    /**
     * 상품 ID로 재고를 반환한다.
     *
     * @param productId 상품 ID
     * @return 재고 응답 DTO
     */
    @GetMapping("/{productId}")
    public ResponseEntity<StockResponse> getStockByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.findByProductId(productId));
    }

    /**
     * 상품의 재고를 입고하여 수량을 증가시킨다.
     *
     * @param productId 상품 ID
     * @param request   입고 요청 DTO
     * @return 200 OK + 갱신된 재고 응답 DTO
     */
    @PostMapping("/{productId}/quantity")
    public ResponseEntity<StockResponse> addStock(@PathVariable Long productId, @Valid @RequestBody StockAddRequest request) {
        return ResponseEntity.ok(stockService.addStock(productId, request.quantity()));
    }
}
