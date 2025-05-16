package com.artmarket.order_service.DTO;

import lombok.NonNull;

public record ShippingRequest(
        @NonNull String recipientName,
        @NonNull String phone,
        @NonNull String city,
        @NonNull String warehouse,
        String shippingProvider // e.g. "NOVA_POSHTA"
) {}
