package com.artmarket.order_service.service;

import com.artmarket.order_service.config.KafkaTopics;
import com.artmarket.order_service.event.*;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.enums.OrderStatus;
import com.artmarket.order_service.model.enums.ShippingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    public void sendOrderCreatedEvent(Order order) {
        sendRecord(kafkaTopics.getOrderCreated(),
                new OrderCreatedEvent(
                        order.getId(),
                        order.getUserId(),
                        order.getStatus().name(),
                        order.getTotalPrice(),
                        order.getItems().size(),
                        order.getShippingInfo().getShippingProvider(),
                        LocalDateTime.now()
                ));
    }

    public void sendOrderStatusChangedEvent(Long orderId, String userId, OrderStatus newStatus) {
        sendRecord( kafkaTopics.getOrderStatusChanged(),
                new OrderStatusChangedEvent(
                        orderId,
                        userId,
                        newStatus.name(),
                        LocalDateTime.now()
                ));
    }

    public void sendOrderUpdatedEvent(Long orderId, String userId, String updateType, Map<String, Object> details) {

        sendRecord(kafkaTopics.getOrderUpdated(),
                new OrderUpdatedEvent(
                        orderId,
                        userId,
                        updateType,
                        details,
                        LocalDateTime.now()
                ));
    }

    public void sendShippingUpdatedEvent(Long orderId, String userId, String provider,
                                         String trackingNumber, ShippingStatus status) {
        sendRecord(
                kafkaTopics.getShippingUpdated(),
                new ShippingUpdatedEvent(
                        orderId,
                        userId,
                        provider,
                        trackingNumber,
                        status.name(),
                        LocalDateTime.now()
                ));
    }

    private void sendRecord(String topic, Object value) {
        String key = value instanceof BaseEvent ?
                ((BaseEvent) value).orderId().toString() :
                UUID.randomUUID().toString();

        try {
            kafkaTemplate.send(topic, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send record to {}: {}", topic, ex.getMessage());
                        } else {
                            log.debug("Successfully sent record to {}: {}", topic, value);
                        }
                    });
        } catch (Exception e) {
            log.error("Error creating Kafka record for topic {}: {}", topic, e.getMessage());
        }
    }
}
