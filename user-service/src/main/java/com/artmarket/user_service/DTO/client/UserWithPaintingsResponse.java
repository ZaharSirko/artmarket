package com.artmarket.user_service.DTO.client;

import org.springframework.data.domain.Page;

import java.util.List;

public record UserWithPaintingsResponse(
        String keycloakId,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        Long createdAt,
        PageResponse paintings
) {}
