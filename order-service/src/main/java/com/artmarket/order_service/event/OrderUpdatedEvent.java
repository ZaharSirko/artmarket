package com.artmarket.order_service.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderUpdatedEvent(
        Long orderId,
        String userId,
        String updateType,
        Map<String, Object> details,
        LocalDateTime eventTime
) implements BaseEvent {}
