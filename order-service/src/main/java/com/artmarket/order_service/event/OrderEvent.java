package com.artmarket.order_service.event;

import com.artmarket.order_service.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderEvent(
        Long orderId,
        String userId,
        OrderStatus status,
        LocalDateTime eventTime,
        Map<String, Object>additionalData
) { }

