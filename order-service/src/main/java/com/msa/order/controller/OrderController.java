package com.msa.order.controller;

import com.msa.order.dto.CreateOrderRequest;
import com.msa.order.dto.OrderResponse;
import com.msa.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 주문 REST 컨트롤러.
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    /**
     * 주문 비즈니스 로직 서비스.
     */
    private final OrderService orderService;

    /**
     * 주문을 생성한다.
     *
     * @param userId  API Gateway가 JWT에서 추출하여 설정한 인증된 사용자 ID
     * @param request 주문 생성 요청 DTO (productId, quantity, totalAmount)
     * @return 201 Created + 생성된 주문 응답 DTO
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestHeader("X-User-Id") String userId,
                                                     @Valid @RequestBody CreateOrderRequest request) {
        log.info("주문 생성 요청 - userId: {}, productId: {}", userId, request.productId());
        OrderResponse response = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 주문 ID로 단건 주문을 조회한다.
     *
     * @param id 조회할 주문 ID
     * @return 200 OK + 주문 응답 DTO, 존재하지 않으면 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        try {
            OrderResponse response = orderService.getOrder(id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 전체 주문 목록을 조회한다.
     *
     * @return 200 OK + 전체 주문 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderService.getAllOrders();
        return ResponseEntity.ok(responses);
    }
}
