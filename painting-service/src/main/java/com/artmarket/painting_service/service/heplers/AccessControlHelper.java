package com.artmarket.painting_service.service.heplers;

import com.artmarket.painting_service.DTO.client.UserResponse;
import com.artmarket.painting_service.DTO.client.UserType;
import com.artmarket.painting_service.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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

    public String getBearerToken() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return "Bearer " + jwtAuth.getToken().getTokenValue();
        }
        throw new AccessDeniedException("Cannot resolve token");
    }


    public String getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("sub");
    }

    public void assertCanCreatePainting() throws AccessDeniedException {
        UserResponse currentUser = userClient.getCurrentUser(getBearerToken());

        List<UserType> userTypes = getUserTypes(currentUser);

        boolean hasPermission = userTypes.stream()
                .anyMatch(PAINTING_CREATORS::contains);

        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission to create paintings.");
        }
    }

    public void checkDeletePermission(String paintingOwnerId) throws AccessDeniedException {
        assertOwnerOrAdmin(paintingOwnerId, "delete");
    }

    public void checkUpdatePermission(String paintingOwnerId) throws AccessDeniedException {
        assertOwnerOrAdmin(paintingOwnerId, "update");
    }

    private void assertOwnerOrAdmin(String paintingOwnerId, String action) throws AccessDeniedException {
        UserResponse currentUser = userClient.getCurrentUser(getBearerToken());

        boolean isOwner = currentUser.keycloakId().equalsIgnoreCase(paintingOwnerId);

        List<UserType> userTypes = getUserTypes(currentUser);

        boolean hasPermission = userTypes.stream().toList().contains(UserType.ADMIN);

        if (!isOwner && !hasPermission) {
            throw new AccessDeniedException("You are not allowed to " + action + " this painting.");
        }
    }

    private static List<UserType> getUserTypes(UserResponse currentUser) {
        return currentUser.roles().stream()
                .map(role -> {
                    try {
                        return UserType.valueOf(role);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
