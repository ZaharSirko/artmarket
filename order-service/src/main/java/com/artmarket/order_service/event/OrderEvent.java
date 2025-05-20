package com.artmarket.order_service.event;

import com.artmarket.order_service.model.enums.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record OrderEvent(
         Long orderId,
         String userId,
         OrderStatus status,
         LocalDateTime eventTime,
         Map<String, Object>additionalData
) {
}
