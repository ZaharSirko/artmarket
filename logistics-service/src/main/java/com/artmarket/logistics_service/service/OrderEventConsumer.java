package com.artmarket.logistics_service.service;


import com.artmarket.DTO.OrderResponse;
import com.artmarket.events.OrderCreatedEvent;
import com.artmarket.events.OrderUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "#{'${kafka.topics.order-created}'}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        OrderResponse order = convertToResponse(event);


    }

    @KafkaListener(topics = "#{'${kafka.topics.order-updated}'}")
    public void handleOrderUpdated(OrderUpdatedEvent event) {
        // Оновити дані в кеші на основі події
        // Можна реалізувати часткове оновлення даних
        log.info("Received update for order {}", event.orderId());
    }

    private OrderResponse convertToResponse(OrderCreatedEvent event) {
        return OrderResponse.builder()
                .id(event.orderId())
                .userId(event.userId())
                .itemsPrice(event.itemsPrice())
                .deliveryPrice(BigDecimal.ZERO)
                .totalPrice(event.itemsPrice())
                .createdAt(Instant.from(event.eventTime()))
                .paintings(event.paintings())
                .build();
    }
}