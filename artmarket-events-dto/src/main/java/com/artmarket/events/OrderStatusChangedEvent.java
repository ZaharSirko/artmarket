package com.artmarket.events;


import com.artmarket.dto.OrderStatus;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderStatusChangedEvent(
        Long orderId,
        String userId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String reason,
        LocalDateTime eventTime
) implements BaseEvent {}


