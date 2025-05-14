package com.artmarket.user_service.DTO;

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
