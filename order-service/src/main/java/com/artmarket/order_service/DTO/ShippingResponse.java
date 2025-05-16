package com.artmarket.order_service.DTO;

import com.artmarket.order_service.model.enums.ShippingStatus;

public record ShippingResponse(
        String shippingProvider,
        String trackingNumber,
        String recipientName,
        String phone,
        String city,
        String warehouse,
        ShippingStatus shippingStatus
) {}
