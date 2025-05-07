package com.artmarket.user_service.DTO.client;

import com.artmarket.user_service.model.UserType;

import java.time.Instant;
import java.util.List;

public record UserWithPaintingsResponse(
        Long id,
        String keycloakId,
        String email,
        String firstName,
        String lastName,
        UserType type,
        Instant createdAt,
        List<PaintingResponse> paintings
) {}
