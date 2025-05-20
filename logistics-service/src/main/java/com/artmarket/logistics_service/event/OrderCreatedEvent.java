package com.artmarket.logistics_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String userId,
        BigDecimal totalPrice,
        String shippingMethod,
        LocalDateTime createdAt
) {}
