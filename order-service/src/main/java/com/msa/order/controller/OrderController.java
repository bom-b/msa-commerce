package com.msa.order.controller;

import com.msa.common.auth.annotation.Authenticated;
import com.msa.common.auth.annotation.CurrentUser;
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
     * @param userId  X-User-Id 헤더에서 추출한 인증된 사용자 ID
     * @param request 주문 생성 요청 DTO (productId, quantity, totalAmount)
     * @return 201 Created + 생성된 주문 응답 DTO
     */
    @Authenticated
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@CurrentUser Long userId,
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
    @Authenticated
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
     * 현재 로그인한 사용자의 주문 목록을 조회한다.
     *
     * @param userId X-User-Id 헤더에서 추출한 인증된 사용자 ID
     * @return 200 OK + 해당 사용자의 주문 응답 DTO 목록
     */
    @Authenticated
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@CurrentUser Long userId) {
        List<OrderResponse> responses = orderService.getAllOrdersByUserId(userId);
        return ResponseEntity.ok(responses);
    }
}
