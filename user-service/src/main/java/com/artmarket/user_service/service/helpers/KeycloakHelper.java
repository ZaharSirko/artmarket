package com.artmarket.user_service.service.helpers;

import com.artmarket.user_service.DTO.UserCreateRequest;
import com.artmarket.user_service.DTO.UserRequest;
import com.artmarket.user_service.config.KeycloakConfig;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class KeycloakHelper {
    private final Keycloak keycloak;
    private final KeycloakConfig config;

    public String extractUserId(Response response) {
        String location = response.getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    public void assignRole(String userId, String role) {
        RoleRepresentation roleRep = keycloak.realm(config.getRealm())
                .roles()
                .get(role)
                .toRepresentation();

        keycloak.realm(config.getRealm())
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(Collections.singletonList(roleRep));
    }

    public void setUserPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        keycloak.realm(config.getRealm())
                .users()
                .get(userId)
                .resetPassword(credential);
    }


    public Response createUser(UserRequest request){
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.lastName()+request.firstName());
        user.setEmail(request.email());
        user.setEnabled(true);
        user.setEmailVerified(false);

        Response response = keycloak.realm(config.getRealm())
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Keycloak user creation failed");
        }
        return response;
    }
}
