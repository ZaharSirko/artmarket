package com.artmarket.dto;

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

