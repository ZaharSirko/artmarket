package com.artmarket.logistics_service.DTO;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

public record ShippingRequest(
        @NonNull String recipientName,
        @NonNull String phone,
        @NonNull String city,
        @NonNull String warehouse,
        @NonNull String shippingMethod,
        @NonNull BigDecimal totalPrice,
        @NonNull List<Long> items

) {}
