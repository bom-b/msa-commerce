package com.msa.order.service;

import com.msa.order.client.StockServiceClient;
import com.msa.order.domain.Order;
import com.msa.order.domain.OrderStatus;
import com.msa.order.dto.CreateOrderRequest;
import com.msa.order.dto.OrderResponse;
import com.msa.common.event.OrderCreatedEvent;
import com.msa.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 주문 비즈니스 로직 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StockServiceClient stockServiceClient;

    /**
     * 주문을 생성하고 {@code order.created} 이벤트를 발행한다.
     *
     * @param request 주문 생성 요청 DTO
     * @param userId  인증된 사용자 ID
     * @return 생성된 주문의 응답 DTO
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        StockServiceClient.StockInfo stockInfo = stockServiceClient.getStock(request.productId());
        BigDecimal totalAmount = stockInfo.price().multiply(BigDecimal.valueOf(request.quantity()));

        Order order = Order.builder()
            .userId(userId)
            .productId(request.productId())
            .productName(stockInfo.productName())
            .quantity(request.quantity())
            .totalAmount(totalAmount)
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 - orderId: {}, userId: {}, productName: {}, totalAmount: {}", savedOrder.getId(), savedOrder.getUserId(), savedOrder.getProductName(), savedOrder.getTotalAmount());

        OrderCreatedEvent event = new OrderCreatedEvent(
            UUID.randomUUID(),
            savedOrder.getId(),
            savedOrder.getUserId(),
            savedOrder.getProductId(),
            savedOrder.getQuantity(),
            savedOrder.getTotalAmount());

        eventPublisher.publishEvent(event);

        return OrderResponse.from(savedOrder);
    }

    /**
     * 주문 ID로 단건 주문을 조회한다.
     *
     * @param orderId 조회할 주문 ID
     * @return 주문 응답 DTO
     * @throws NoSuchElementException 해당 ID의 주문이 없을 경우
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));
        return OrderResponse.from(order);
    }

    /**
     * 특정 사용자의 주문 목록을 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 주문 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId).stream().map(OrderResponse::from).toList();
    }

    /**
     * 주문 상태를 COMPLETED로 전이한다.
     *
     * @param orderId 완료 처리할 주문 ID
     * @throws NoSuchElementException 해당 ID의 주문이 없을 경우
     */
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));

        order.complete();
        log.info("주문 완료 처리 - orderId: {}", orderId);
    }

    /**
     * 주문 상태를 CANCELLED로 전이하고 실패 사유를 기록한다.
     *
     * @param orderId 취소 처리할 주문 ID
     * @param reason  취소 사유
     * @throws NoSuchElementException 해당 ID의 주문이 없을 경우
     */
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));

        order.cancel(reason);
        log.info("주문 취소 처리 - orderId: {}, reason: {}", orderId, reason);
    }

}
