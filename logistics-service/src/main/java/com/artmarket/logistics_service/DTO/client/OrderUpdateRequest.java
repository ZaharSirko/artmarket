package com.artmarket.logistics_service.DTO.client;

import java.math.BigDecimal;

public record OrderUpdateRequest(
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        ShippingUpdate shipping
) {}
