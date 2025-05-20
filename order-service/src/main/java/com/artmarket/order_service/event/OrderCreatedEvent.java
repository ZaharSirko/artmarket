package com.artmarket.order_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String userId,
        String status,
        BigDecimal totalPrice,
        int itemsCount,
        String shippingProvider,
        LocalDateTime eventTime
) implements BaseEvent{}
