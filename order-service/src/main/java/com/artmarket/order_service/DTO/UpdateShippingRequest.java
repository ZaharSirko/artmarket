package com.artmarket.order_service.DTO;

public record UpdateShippingRequest(
        String shippingProvider,
        String trackingNumber,
        String shippingStatus
) {}
