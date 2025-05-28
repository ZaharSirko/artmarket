package com.artmarket.order_service.service;

import com.artmarket.DTO.*;
import com.artmarket.events.OrderUpdatedEvent;

import com.artmarket.order_service.DTO.OrderItemRequest;
import com.artmarket.order_service.DTO.OrderRequest;
import com.artmarket.order_service.event.OrderPaintingUpdatedEvent;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;

import com.artmarket.order_service.repository.OrderRepository;
import com.artmarket.order_service.service.heplers.OrderServiceHelper;
import com.artmarket.order_service.service.heplers.OrderValidator;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


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
        UserResponse user = helper.getCurrentUser(bearerToken);

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
        kafkaProducer.sendOrderCreatedEvent(savedOrder,paintings);
        recordOrderCreatedMetrics(savedOrder);

        log.info("Created new order {} for user {}", order.getId(), user.keycloakId());
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

        kafkaProducer.sendOrderPaintingUpdateEvent(updatedOrder, List.of(painting), OrderPaintingUpdatedEvent.ActionType.ADDED);

        log.info("Added painting {} to order {}", painting.id(), orderId);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse removePaintingFromOrder(Long orderId, OrderItemRequest request, String bearerToken) {
        Order order = helper.getOrderWithValidation(orderId, bearerToken);
        OrderItem itemToRemove = helper.findOrderItem(order, request.paintingId());

        order.getItems().remove(itemToRemove);
        order.setItemsPrice(helper.recalculateItemsPrice(order));
        var removedPainting = PaintingResponse.builder()
                .id(itemToRemove.getPaintingId())
                .price(itemToRemove.getPrice())
                .build();

        Order updatedOrder = orderRepository.save(order);

        kafkaProducer.sendOrderPaintingUpdateEvent(updatedOrder, List.of(removedPainting), OrderPaintingUpdatedEvent.ActionType.REMOVED);

        log.info("Removed painting {} from order {}", request.paintingId(), orderId);
        return mapToResponse(updatedOrder);
    }


    @Transactional
    public void updateOrderFromEvent(OrderUpdatedEvent event) {
        log.info("Processing OrderUpdatedEvent for order {}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> {
                    log.error("Order not found for event: {}", event.orderId());
                    return new EntityNotFoundException("Order not found");
                });

        order.setDeliveryPrice(event.deliveryPrice());
        order.setTotalPrice(event.totalPrice());

        if (event.shipping() != null) {
            order.setShippingInfo(helper.buildShippingInfoFromEvent(event.shipping()));
        }
        order.setStatus(OrderStatus.PROCESSED);

        orderRepository.save(order);
        log.info("Order {} updated from Kafka event", order.getId());

        kafkaProducer.sendOrder(mapToResponse(order));
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
                helper.getCurrentUser(bearerToken).keycloakId(),
                updatedOrder.getStatus(),
                OrderStatus.CANCELLED,
                "Cancelled by user"
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
                helper.getCurrentUser(bearerToken).keycloakId(),
                OrderStatus.PAID,
                OrderStatus.COMPLETED,
                "Order successfully completed"
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
        String userId = helper.getCurrentUser(bearerToken).keycloakId();
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
