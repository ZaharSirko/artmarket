package com.artmarket.user_service.DTO;

import lombok.NonNull;

@NonNull
public record UserRegistrationRequest(
       String username,
       String email,
       String firstName,
       String lastName,
       String password
) {}
