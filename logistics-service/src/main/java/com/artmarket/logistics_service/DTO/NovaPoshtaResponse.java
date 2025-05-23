package com.artmarket.logistics_service.DTO;

import java.util.List;
import java.util.Map;

public record NovaPoshtaResponse(
        boolean success,
        List<Map<String, Object>> data,
        List<String> errors,
        List<String> warnings,
        Object info
) {}



