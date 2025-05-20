package com.artmarket.logistics_service.DTO;

public record CreateTTNResponse(
        String Ref,
        String CostOnSite,
        String EstimatedDeliveryDate
) {}
