package com.artmarket.order_service.DTO;

import com.artmarket.order_service.model.enums.ShippingStatus;

public record ShippingResponse(
        String shippingProvider,
        String trackingNumber,
        String city,
        String warehouse,
        String recipientFullName,
        String phone,
        String email,
        ShippingStatus shippingStatus
) {}
