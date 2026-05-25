package com.msa.stock.controller;

import com.msa.stock.dto.StockResponse;
import com.msa.stock.service.StockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
     * @return 200 OK + 재고 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<StockResponse>> getStocks() {
        return ResponseEntity.ok(stockService.findAll());
    }
}
