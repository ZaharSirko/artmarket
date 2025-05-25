package com.artmarket.order_service.DTO;

import java.math.BigDecimal;

public record OrderUpdateRequest(
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        ShippingUpdate shipping
) {}
