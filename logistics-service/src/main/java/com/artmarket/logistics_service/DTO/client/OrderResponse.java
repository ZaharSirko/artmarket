package com.artmarket.logistics_service.DTO.client;

import com.artmarket.logistics_service.DTO.ShippingRequest;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String userId,
        BigDecimal itemsPrice,
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        Instant createdAt,
        List<PaintingResponse> paintings,
        ShippingResponse shipping
)  {}