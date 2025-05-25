package com.artmarket.order_service.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderCreatedEvent(
        Long orderId,
        String userId,
        String status,
        BigDecimal itemsPrice,
        int itemsCount,
        LocalDateTime eventTime
) implements BaseEvent {}