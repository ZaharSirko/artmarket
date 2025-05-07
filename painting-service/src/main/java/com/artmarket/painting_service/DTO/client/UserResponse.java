package com.artmarket.painting_service.DTO.client;

public record UserResponse(
        Long id,
        String keycloakId,
        String email,
        String firstName,
        String lastName,
        UserType type
) {}
