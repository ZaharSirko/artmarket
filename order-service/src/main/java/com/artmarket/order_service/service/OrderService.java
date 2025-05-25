package com.artmarket.order_service.service;

import com.artmarket.order_service.DTO.*;
import com.artmarket.order_service.DTO.client.PaintingResponse;
import com.artmarket.order_service.DTO.client.UserResponse;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;
import com.artmarket.order_service.model.enums.OrderStatus;
import com.artmarket.order_service.repository.OrderRepository;
import com.artmarket.order_service.service.heplers.OrderServiceHelper;
import com.artmarket.order_service.service.heplers.OrderValidator;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderServiceHelper helper;
    private final OrderValidator validator;
    private final KafkaOrderEventProducer kafkaProducer;
    private final MeterRegistry meterRegistry;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String bearerToken) {
        validator.validateOrderRequest(request);
        UserResponse user = getUser(bearerToken);

        List<PaintingResponse> paintings = helper.getPaintingsForOrder(request);
        BigDecimal itemsPrice = helper.calculateItemsPrice(paintings);

        Order order = Order.builder()
                .userId(user.keycloakId())
                .status(OrderStatus.NEW)
                .itemsPrice(itemsPrice)
                .build();

        order.setItems(helper.buildOrderItems(paintings, order));
        order.setItemsPrice(itemsPrice);

        Order savedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderCreatedEvent(savedOrder);
        recordOrderCreatedMetrics(savedOrder);

        log.info("Created new order {} for user {}", order.getId(), user.keycloakId());;
        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse addPaintingToOrder(Long orderId, OrderItemRequest request, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        PaintingResponse painting = helper.getValidatedPainting(request.paintingId());

        OrderItem item = helper.buildOrderItem(painting, order);
        order.getItems().add(item);
        order.setItemsPrice(helper.recalculateItemsPrice(order));

        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderUpdatedEvent(
                updatedOrder.getId(),
                helper.getCurrentUser(bearerToken).keycloakId(),
                "PAINTING_ADDED",
                Map.of(
                        "paintingId", painting.id(),
                        "newItemsPrice", updatedOrder.getItemsPrice()
                )
        );

        log.info("Added painting {} to order {}", painting.id(), orderId);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse removePaintingFromOrder(Long orderId, OrderItemRequest request, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        OrderItem itemToRemove = helper.findOrderItem(order, request.paintingId());

        order.getItems().remove(itemToRemove);
        order.setItemsPrice(helper.recalculateItemsPrice(order));

        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderUpdatedEvent(
                updatedOrder.getId(),
                helper.getCurrentUser(bearerToken).keycloakId(),
                "PAINTING_REMOVED",
                Map.of(
                        "paintingId", request.paintingId(),
                        "newTotalPrice", updatedOrder.getTotalPrice()
                )
        );

        log.info("Removed painting {} from order {}", request.paintingId(), orderId);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);

        order.setDeliveryPrice(request.deliveryPrice());
        order.setTotalPrice(request.totalPrice());
        order.setShippingInfo(helper.buildShippingInfo(request));

        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendShippingUpdatedEvent(
                updatedOrder.getId(),
                getUser(bearerToken).keycloakId(),
                updatedOrder.getShippingInfo()
        );

        recordCounter("order.updated", "orderId", orderId.toString());
        log.info("Updated shipping info for order {}", orderId);

        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return mapToResponse(order);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderStatusChangedEvent(
                updatedOrder.getId(),
                getUser(bearerToken).keycloakId(),
                OrderStatus.CANCELLED
        );

        recordCounter("order.cancelled", "orderId", orderId.toString());
        log.info("Cancelled order {}", orderId);

        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse completeOrder(Long orderId, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        if (order.getStatus() != OrderStatus.PAID) {
            return mapToResponse(order);
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderStatusChangedEvent(
                updatedOrder.getId(),
                getUser(bearerToken).keycloakId(),
                OrderStatus.COMPLETED
        );

        recordCounter("order.completed", "orderId", orderId.toString());
        meterRegistry.timer("order.processing.time").record(Duration.between(order.getCreatedAt(), LocalDateTime.now()));

        log.info("Completed order {}", orderId);
        return mapToResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String bearerToken) {
        return mapToResponse(helper.getOrderWithValidation(id, bearerToken));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCurrentUser(String bearerToken) {
        String userId = getUser(bearerToken).keycloakId();
        return orderRepository.findAllByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // === Helpers ===

    private void recordOrderCreatedMetrics(Order order) {
        meterRegistry.counter("order.created.count").increment();
        meterRegistry.gauge("order.items.count", order.getItems().size());
        meterRegistry.gauge("order.items.price", order.getItemsPrice().doubleValue());
    }

    private void recordCounter(String name, String... tags) {
        meterRegistry.counter(name, tags).increment();
    }


    private UserResponse getUser(String bearerToken) {
        return helper.getCurrentUser(bearerToken);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .itemsPrice(order.getItemsPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .paintings(helper.getPaintingsForOrderItems(order.getItems()))
                .shipping(helper.buildShippingResponse(order.getShippingInfo()))
                .build();
    }
}
