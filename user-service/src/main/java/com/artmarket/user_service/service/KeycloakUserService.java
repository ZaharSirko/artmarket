package com.artmarket.user_service.service;

import com.artmarket.user_service.DTO.UserRegistrationRequest;
import com.artmarket.user_service.DTO.UserResponse;
import com.artmarket.user_service.DTO.client.PageResponse;
import com.artmarket.user_service.DTO.client.PaintingResponse;
import com.artmarket.user_service.DTO.client.UserWithPaintingsResponse;
import com.artmarket.user_service.client.PaintingClient;
import com.artmarket.user_service.config.KeycloakConfig;
import com.artmarket.user_service.service.helpers.KeycloakHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {
    private static final Logger log = LoggerFactory.getLogger(KeycloakUserService.class);
    private final KeycloakHelper keycloakHelper;
    private final PaintingClient paintingClient;
    private final Keycloak keycloak;
    private final KeycloakConfig config;

    public UserRegistrationRequest createUser(UserRegistrationRequest request) {

        UserRepresentation user=new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmailVerified(false);

        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(request.password());
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        List<CredentialRepresentation> list = new ArrayList<>();
        list.add(credentialRepresentation);
        user.setCredentials(list);

        UsersResource usersResource = keycloakHelper.getUsersResource();

        Response response = usersResource.create(user);

        if(Objects.equals(201,response.getStatus())){

//            List<UserRepresentation> representationList = usersResource.searchByUsername(request.username(), true);
//            if(!CollectionUtils.isEmpty(representationList)){
//                UserRepresentation userRepresentation1 = representationList.stream().filter(userRepresentation -> Objects.equals(false, userRepresentation.isEmailVerified())).findFirst().orElse(null);
//                assert userRepresentation1 != null;
//                emailVerification(userRepresentation1.getId());
//            }
            log.info("User created successfully");
            return  request;
        }

        return null;
    }


    public void assignRole(String userId, String roleName) {
        UserResource userResource = keycloakHelper.getUserResource(userId);
        RolesResource rolesResource = keycloakHelper.getRolesResource();
        RoleRepresentation representation = rolesResource.get(roleName).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(representation));
    }

//    public void assignRole(String userId, String roleName) {
//        RealmResource realmResource = keycloakHelper.getRealmResource();
//
//        RoleRepresentation role = realmResource.roles()
//                .get(roleName)
//                .toRepresentation();
//
//        if (role == null) {
//            throw new IllegalArgumentException("Role '" + roleName + "' not found in realm.");
//        }
//
//        realmResource.users()
//                .get(userId)
//                .roles()
//                .realmLevel()
//                .add(Collections.singletonList(role));
//    }









    public void emailVerification(String userId){

        UsersResource usersResource = keycloakHelper.getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }

    public void updatePassword(String userId) {

        UserResource userResource = keycloakHelper.getUserResource(userId);
        List<String> actions= new ArrayList<>();
        actions.add("UPDATE_PASSWORD");
        userResource.executeActionsEmail(actions);

    }

    public void updatePassword(String resetPassword, String userId) {

        UserResource userResource = keycloakHelper.getUserResource(userId);
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(resetPassword);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        userResource.resetPassword(credentialRepresentation);
    }

    public void deleteUserById(String userId) {
        keycloakHelper.getUsersResource().delete(userId);
    }

    public UserResponse getUserById(String userId) {
        var user = keycloakHelper.getUsersResource().get(userId).toRepresentation();
        List<String> filteredRoles = keycloakHelper.getFilteredUserRoles(userId);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                filteredRoles,
                user.getCreatedTimestamp()
        );
    }


    public UserWithPaintingsResponse getUserWithPaintings(String userId) {
        PageResponse paintings = paintingClient.getUserPaintings(userId);
        var user = keycloakHelper.getUsersResource().get(userId).toRepresentation();
        return new UserWithPaintingsResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRealmRoles(),
                user.getCreatedTimestamp(),
                paintings
                );
    }

}

