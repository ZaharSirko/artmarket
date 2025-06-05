package com.artmarket.events;


import com.artmarket.dto.OrderStatus;
import com.artmarket.dto.PaintingResponse;
import com.artmarket.dto.ShippingResponse;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderEvent(
        Long orderId,
        String userId,
        OrderStatus status,
        BigDecimal itemsPrice,
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        List<PaintingResponse> paintings,
        ShippingResponse shipping,
        LocalDateTime eventTime
) implements BaseEvent {}


