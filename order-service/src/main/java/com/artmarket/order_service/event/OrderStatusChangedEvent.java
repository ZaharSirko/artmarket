package com.artmarket.order_service.event;

import java.time.LocalDateTime;

public record OrderStatusChangedEvent(
        Long orderId,
        String userId,
        String newStatus,
        LocalDateTime eventTime
) implements BaseEvent {}

