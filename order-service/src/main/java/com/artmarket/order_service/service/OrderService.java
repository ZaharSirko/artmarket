package com.artmarket.order_service.service;

import com.artmarket.order_service.DTO.*;
import com.artmarket.order_service.DTO.client.PaintingResponse;
import com.artmarket.order_service.DTO.client.UserResponse;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;
import com.artmarket.order_service.model.ShippingInfo;
import com.artmarket.order_service.model.enums.OrderStatus;
import com.artmarket.order_service.model.enums.ShippingStatus;
import com.artmarket.order_service.repository.OrderRepository;
import com.artmarket.order_service.service.heplers.OrderServiceHelper;
import com.artmarket.order_service.service.heplers.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String bearerToken) {
        validator.validateOrderRequest(request);

        UserResponse user = helper.getCurrentUser(bearerToken);
        List<PaintingResponse> paintings = helper.getPaintingsForOrder(request);
        BigDecimal totalPrice = helper.calculateTotalPrice(paintings);

        Order order = Order.builder()
                .userId(user.keycloakId())
                .status(OrderStatus.NEW)
                .totalPrice(totalPrice)
                .shippingInfo(helper.buildShippingInfo(request.shipping()))
                .build();

        order.setItems(helper.buildOrderItems(paintings, order));
        Order savedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderCreatedEvent(savedOrder);

        log.info("Created new order {} for user {}", savedOrder.getId(), user.keycloakId());

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse addPaintingToOrder(Long orderId, OrderItemRequest request, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        PaintingResponse painting = helper.getValidatedPainting(request.paintingId());

        OrderItem item = helper.buildOrderItem(painting, order);
        order.getItems().add(item);
        order.setTotalPrice(helper.recalculateTotalPrice(order));

        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderUpdatedEvent(
                updatedOrder.getId(),
                helper.getCurrentUser(bearerToken).keycloakId(),
                "PAINTING_ADDED",
                Map.of(
                        "paintingId", painting.id(),
                        "newTotalPrice", updatedOrder.getTotalPrice()
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
        order.setTotalPrice(helper.recalculateTotalPrice(order));

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
    public OrderResponse updateShipping(Long orderId, UpdateShippingRequest request, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        ShippingInfo shipping = ShippingInfo.builder()
                .shippingProvider(request.shippingProvider())
                .trackingNumber(request.trackingNumber())
                .shippingStatus(ShippingStatus.valueOf(request.shippingStatus()))
                .build();
        order.setShippingInfo(shipping);


        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendShippingUpdatedEvent(
                orderId,
                helper.getCurrentUser(bearerToken).keycloakId(),
                request.shippingProvider(),
                request.trackingNumber(),
                ShippingStatus.valueOf(request.shippingStatus())
        );

        log.info("Updated shipping for order {}", orderId);
        return mapToResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String bearerToken) {
        Order order = helper.getOrderWithValidation(id, bearerToken);
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCurrentUser(String bearerToken) {
        String userId = helper.getCurrentUser(bearerToken).keycloakId();
        return orderRepository.findAllByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .paintings(helper.getPaintingsForOrderItems(order.getItems()))
                .shipping(helper.buildShippingResponse(order.getShippingInfo()))
                .build();
    }
}
