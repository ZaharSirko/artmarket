package com.artmarket.events;

import com.artmarket.dto.OrderStatus;
import com.artmarket.dto.PaintingResponse;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderCreatedEvent(
        Long orderId,
        String userId,
        OrderStatus status,
        BigDecimal itemsPrice,
        int itemsCount,
        List<PaintingResponse> paintings,
        LocalDateTime eventTime
) implements BaseEvent {}
