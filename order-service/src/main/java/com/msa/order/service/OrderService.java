package com.msa.order.service;

import com.msa.order.client.StockServiceClient;
import com.msa.order.domain.Order;
import com.msa.order.domain.OrderFailureReason;
import com.msa.order.domain.OrderStatus;
import com.msa.order.dto.CreateOrderRequest;
import com.msa.order.dto.OrderResponse;
import com.msa.order.dto.PageResponse;
import com.msa.common.event.OrderCreatedEvent;
import com.msa.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Long totalAmount = stockInfo.price() * request.quantity();

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

    /** 페이지당 최대 조회 가능 건수. */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 특정 사용자의 주문 목록을 페이지네이션하여 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @param page   0-based 페이지 번호
     * @param size   페이지당 데이터 수 (최대 {@value MAX_PAGE_SIZE})
     * @return 페이지네이션된 주문 응답 DTO
     * @throws IllegalArgumentException size가 1 미만이거나 최대값 초과 시
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrdersByUserId(Long userId, int page, int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size는 1 이상 " + MAX_PAGE_SIZE + " 이하여야 합니다.");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponse> result = orderRepository.findAllByUserId(userId, pageable).map(OrderResponse::from);
        return PageResponse.from(result);
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
    public void cancelOrder(Long orderId, OrderFailureReason reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));

        order.cancel(reason);
        log.info("주문 취소 처리 - orderId: {}, reason: {}", orderId, reason);
    }

}
