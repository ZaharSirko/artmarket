package com.artmarket.user_service.DTO;

import com.artmarket.user_service.model.UserType;
import lombok.NonNull;

@NonNull
public record UserCreateRequest(
       String email,
       String firstName,
       String lastName,
       UserType type,
       String password
) implements UserRequest{}
