package com.artmarket.order_service.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderStatusChangedEvent(
        Long orderId,
        String userId,
        String newStatus,
        LocalDateTime eventTime
) implements BaseEvent {}

