package com.artmarket.painting_service.service.heplers;

import com.artmarket.painting_service.DTO.client.UserResponse;
import com.artmarket.painting_service.DTO.client.UserType;
import com.artmarket.painting_service.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AccessControlHelper {
    private final UserClient userClient;


    private static final Set<UserType> PAINTING_CREATORS = EnumSet.of(
            UserType.ARTIST,
            UserType.GALLERY_OWNER,
            UserType.ADMIN
    );

    private String getBearerToken() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("No authentication found in security context.");
        }
        return "Bearer " + authentication.getCredentials().toString();
    }

    public Long getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("sub");
    }

    public void assertCanCreatePainting() throws AccessDeniedException {
        UserResponse currentUser = userClient.getCurrentUser(getBearerToken());
        if (!PAINTING_CREATORS.contains(currentUser.type())) {
            throw new AccessDeniedException("You do not have permission to create paintings.");
        }
    }


    public void checkDeletePermission(Long paintingOwnerId) throws AccessDeniedException {
        UserResponse currentUser = userClient.getCurrentUser(getBearerToken());

        boolean isOwner = currentUser.id().equals(paintingOwnerId);
        boolean isAdmin = currentUser.type() == UserType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to delete this painting.");
        }
    }

    public void checkUpdatePermission(Long paintingOwnerId) throws AccessDeniedException {
        UserResponse currentUser = userClient.getCurrentUser(getBearerToken());

        boolean isOwner = currentUser.id().equals(paintingOwnerId);
        boolean isAdmin = currentUser.type() == UserType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to update this painting.");
        }
    }
}
