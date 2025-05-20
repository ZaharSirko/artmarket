package com.artmarket.logistics_service.DTO.client;

public record UpdateShippingRequest(
        String shippingProvider,
        String trackingNumber,
        String shippingStatus
) {}
