package com.artmarket.logistics_service.DTO;

public record DocumentResponse(
        String Ref,
        Double CostOnSite,
        String EstimatedDeliveryDate,
        String IntDocNumber
) {}
