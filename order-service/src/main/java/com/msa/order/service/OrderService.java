package com.msa.order.service;

import com.msa.order.domain.Order;
import com.msa.order.domain.OrderStatus;
import com.msa.order.dto.CreateOrderRequest;
import com.msa.order.dto.OrderResponse;
import com.msa.order.dto.event.OrderCreatedEvent;
import com.msa.order.kafka.producer.OrderEventProducer;
import com.msa.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 주문 데이터 접근 레포지토리.
     */
    private final OrderRepository orderRepository;

    /**
     * 주문 이벤트 Kafka 발행자.
     */
    private final OrderEventProducer orderEventProducer;

    /**
     * 주문을 생성하고 {@code order.created} 이벤트를 발행한다.
     *
     * @param request 주문 생성 요청 DTO
     * @param userId  인증된 사용자 ID
     * @return 생성된 주문의 응답 DTO
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userId) {
        Order order =
            Order.builder()
                .userId(userId)
                .productId(request.productId())
                .quantity(request.quantity())
                .status(OrderStatus.PENDING)
                .totalAmount(request.totalAmount())
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 - orderId: {}, userId: {}", savedOrder.getId(), savedOrder.getUserId());

        OrderCreatedEvent event =
            new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getTotalAmount());

        orderEventProducer.sendOrderCreated(event);

        return OrderResponse.from(savedOrder);
    }

    /**
     * 주문 ID로 단건 주문을 조회한다.
     *
     * @param orderId 조회할 주문 ID
     * @return 주문 응답 DTO
     * @throws NoSuchElementException 해당 ID의 주문이 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order =
            orderRepository
                .findById(orderId)
                .orElseThrow(
                    () -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));
        return OrderResponse.from(order);
    }

    /**
     * 전체 주문 목록을 조회한다.
     *
     * @return 전체 주문 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    /**
     * 주문 상태를 COMPLETED로 변경한다.
     *
     * @param orderId 완료 처리할 주문 ID
     * @throws NoSuchElementException 해당 ID의 주문이 존재하지 않을 경우
     */
    @Transactional
    public void completeOrder(Long orderId) {
        Order order =
            orderRepository
                .findById(orderId)
                .orElseThrow(
                    () -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));
        order.updateStatus(OrderStatus.COMPLETED);
        log.info("주문 완료 처리 - orderId: {}", orderId);
    }

    /**
     * 주문 상태를 CANCELLED로 변경한다.
     *
     * @param orderId 취소 처리할 주문 ID
     * @throws NoSuchElementException 해당 ID의 주문이 존재하지 않을 경우
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order =
            orderRepository
                .findById(orderId)
                .orElseThrow(
                    () -> new NoSuchElementException("주문을 찾을 수 없습니다. orderId: " + orderId));
        order.updateStatus(OrderStatus.CANCELLED);
        log.info("주문 취소 처리 - orderId: {}", orderId);
    }
}
