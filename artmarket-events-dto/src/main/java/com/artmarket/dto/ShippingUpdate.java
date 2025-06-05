package com.artmarket.dto;

public record ShippingUpdate(
        String shippingProvider,
        String trackingNumber,
        String city,
        String warehouse,
        String recipientFullName,
        String phone,
        String email,
        ShippingStatus shippingStatus
) {}
