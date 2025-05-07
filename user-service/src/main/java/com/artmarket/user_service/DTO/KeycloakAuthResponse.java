package com.artmarket.user_service.DTO;

public record KeycloakAuthResponse(
        String access_token,
        String refresh_token,
        int expires_in
) {}
