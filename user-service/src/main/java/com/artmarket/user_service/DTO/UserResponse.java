package com.artmarket.user_service.DTO;

import com.artmarket.user_service.model.UserType;
import java.time.Instant;


public record UserResponse(
        Long id,
        String keycloakId,
        String email,
        String firstName,
        String lastName,
        UserType type,
        Instant createdAt
) {}
