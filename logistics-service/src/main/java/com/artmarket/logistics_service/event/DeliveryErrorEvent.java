package com.artmarket.logistics_service.event;

import java.time.LocalDateTime;

public record DeliveryErrorEvent(
        Long orderId,
        String errorMessage,
        LocalDateTime timestamp
) {}
