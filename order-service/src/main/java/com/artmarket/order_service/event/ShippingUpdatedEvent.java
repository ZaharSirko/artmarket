package com.artmarket.order_service.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record ShippingUpdatedEvent(
        Long orderId,
        String userId,
        String provider,
        String trackingNumber,
        String status,
        LocalDateTime eventTime
) implements BaseEvent {}
