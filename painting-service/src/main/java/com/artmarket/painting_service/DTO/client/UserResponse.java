package com.artmarket.painting_service.DTO.client;

import java.util.List;


public record UserResponse(
        String keycloakId,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        Long createdAt
) {}
