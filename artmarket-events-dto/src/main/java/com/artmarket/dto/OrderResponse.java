package com.artmarket.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record OrderResponse(
        Long id,
        String userId,
        OrderStatus status,
        BigDecimal itemsPrice,
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        Instant createdAt,
        List<PaintingResponse> paintings,
        ShippingResponse shipping
)  {}