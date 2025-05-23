package com.artmarket.logistics_service.DTO;

public record DocumentResponse(
        String Ref,
        String IntDocNumber,
        String EstimatedDeliveryDate
) {}
