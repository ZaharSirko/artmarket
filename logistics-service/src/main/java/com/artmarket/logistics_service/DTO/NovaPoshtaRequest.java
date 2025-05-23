package com.artmarket.logistics_service.DTO;

import java.util.HashMap;
import java.util.Map;

public record NovaPoshtaRequest(
        String apiKey,
        String modelName,
        String calledMethod,
        Map<String, Object> methodProperties
) {
    public NovaPoshtaRequest {
        methodProperties = methodProperties != null ? methodProperties : new HashMap<>();
    }
}

