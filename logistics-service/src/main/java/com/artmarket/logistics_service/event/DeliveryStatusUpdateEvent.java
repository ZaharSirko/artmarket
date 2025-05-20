package com.artmarket.logistics_service.event;

import java.time.LocalDateTime;

public record DeliveryStatusUpdateEvent(
        Long orderId,
        String status,
        String trackingNumber,
        LocalDateTime timestamp
) {}

