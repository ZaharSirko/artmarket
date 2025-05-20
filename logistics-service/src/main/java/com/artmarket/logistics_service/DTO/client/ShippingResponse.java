package com.artmarket.logistics_service.DTO.client;

public record ShippingResponse(
        String shippingProvider,
        String trackingNumber,
        String recipientName,
        String phone,
        String city,
        String warehouse,
        String shippingStatus
) {}
