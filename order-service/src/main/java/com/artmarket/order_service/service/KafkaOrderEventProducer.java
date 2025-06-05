package com.artmarket.order_service.service;

import com.artmarket.dto.OrderResponse;
import com.artmarket.dto.OrderStatus;
import com.artmarket.dto.PaintingResponse;
import com.artmarket.dto.ShippingUpdate;
import com.artmarket.config.KafkaTopics;
import com.artmarket.events.OrderCreatedEvent;
import com.artmarket.events.OrderEvent;
import com.artmarket.events.OrderStatusChangedEvent;
import com.artmarket.events.OrderUpdatedEvent;


import com.artmarket.order_service.event.OrderPaintingUpdatedEvent;
import com.artmarket.order_service.model.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    public void sendOrderCreatedEvent(Order order, List<PaintingResponse> paintings) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getItemsPrice(),
                order.getItems().size(),
                paintings,
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getOrderCreated(), event);
        log.info("Sent OrderCreatedEvent for order {}", order.getId());
    }

    public void sendOrderStatusChangedEvent(Long orderId, String userId, OrderStatus previousStatus, OrderStatus newStatus, String reason) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                orderId,
                userId,
                previousStatus,
                newStatus,
                reason,
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getOrderStatusChanged(), event);
        log.info("Sent OrderStatusChangedEvent for order {} ({} -> {})", orderId, previousStatus, newStatus);
    }

    public void sendOrderUpdatedEvent(Order order, ShippingUpdate shippingUpdate) {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                order.getId(),
                order.getUserId(),
                order.getDeliveryPrice(),
                order.getTotalPrice(),
                shippingUpdate,
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getOrderUpdated(), event);
        log.info("Sent OrderUpdatedEvent for order {}", order.getId());
    }

    public void sendOrderPaintingUpdateEvent(Order order,
                                             List<PaintingResponse> paintings,
                                             OrderPaintingUpdatedEvent.ActionType action) {
        OrderPaintingUpdatedEvent event = new OrderPaintingUpdatedEvent(
                order.getId(),
                order.getUserId(),
                order.getItemsPrice(),
                paintings.stream()
                        .map(p -> new OrderPaintingUpdatedEvent.PaintingItem(p.id(), p.price()))
                        .toList(),
                action,
                LocalDateTime.now()
        );

        kafkaTemplate.send("order-painting-updated-topic", event);
    }

    public void sendOrder(OrderResponse orderResponse) {
        OrderEvent orderEvent = new OrderEvent(
                orderResponse.id(),
                orderResponse.userId(),
                orderResponse.status(),
                orderResponse.itemsPrice(),
                orderResponse.deliveryPrice(),
                orderResponse.totalPrice(),
                orderResponse.paintings(),
                orderResponse.shipping(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("orders", orderEvent);
        log.info("Sent updated order {} to Kafka topic '{}'", orderResponse.id(), "orders");
    }



}
