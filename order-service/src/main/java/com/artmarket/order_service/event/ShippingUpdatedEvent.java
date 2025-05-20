package com.artmarket.order_service.event;

import java.time.LocalDateTime;

public record ShippingUpdatedEvent(
        Long orderId,
        String userId,
        String provider,
        String trackingNumber,
        String status,
        LocalDateTime eventTime
) implements BaseEvent {}
