package com.artmarket.order_service.service;

import com.artmarket.order_service.config.KafkaTopics;
import com.artmarket.order_service.event.*;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.ShippingInfo;
import com.artmarket.order_service.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    public void sendOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getStatus().name(),
                order.getItemsPrice(),
                order.getItems().size(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getOrderCreated(), event);
        log.info("Sent OrderCreatedEvent for order {}", order.getId());
    }

    public void sendOrderStatusChangedEvent(Long orderId, String userId, OrderStatus newStatus) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                orderId,
                userId,
                newStatus.name(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getOrderStatusChanged(), event);
        log.info("Sent OrderStatusChangedEvent for order {}", orderId);
    }

    public void sendOrderUpdatedEvent(Long orderId, String userId, String updateType, Map<String, Object> details) {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                orderId,
                userId,
                updateType,
                details,
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getOrderUpdated(), event);
        log.info("Sent OrderUpdatedEvent for order {}", orderId);
    }

    public void sendShippingUpdatedEvent(Long orderId, String userId, ShippingInfo shippingInfo) {
        ShippingUpdatedEvent event = new ShippingUpdatedEvent(
                orderId,
                userId,
                shippingInfo.getShippingProvider(),
                shippingInfo.getTrackingNumber(),
                shippingInfo.getShippingStatus().name(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getShippingUpdated(), event);
        log.info("Sent ShippingUpdatedEvent for order {}", orderId);
    }
}
