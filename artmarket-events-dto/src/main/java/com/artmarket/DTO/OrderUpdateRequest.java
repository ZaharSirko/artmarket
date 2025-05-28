package com.artmarket.DTO;

import java.math.BigDecimal;

public record OrderUpdateRequest(
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        ShippingUpdate shipping
) {}
