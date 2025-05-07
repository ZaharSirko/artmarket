package com.artmarket.user_service.DTO;

import lombok.NonNull;

@NonNull
public record OauthUserRequest (
        String email,
        String firstName,
        String lastName
) implements UserRequest{}
