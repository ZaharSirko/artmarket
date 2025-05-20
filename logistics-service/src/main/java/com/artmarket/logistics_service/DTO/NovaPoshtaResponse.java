package com.artmarket.logistics_service.DTO;

import java.util.List;

public record NovaPoshtaResponse(
        boolean success,
        List<Object> data,
        List<String> errors,
        List<String> warnings,
        List<String> info
) {}


