package com.artmarket.order_service.event;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderUpdatedEvent(
        Long orderId,
        String userId,
        String updateType,
        Map<String, Object> details,
        LocalDateTime eventTime
) implements BaseEvent {}
