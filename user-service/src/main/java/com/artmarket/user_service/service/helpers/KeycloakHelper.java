package com.artmarket.user_service.service.helpers;


import com.artmarket.user_service.DTO.UserType;
import com.artmarket.user_service.config.KeycloakConfig;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;

import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class KeycloakHelper {
    private final Keycloak keycloak;
    private final KeycloakConfig config;


    public UsersResource getUsersResource() {
        RealmResource realm1 = getRealmResource();
        return realm1.users();
    }


    public UserResource getUserResource(String userId){
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }


    public RolesResource getRolesResource(){
        return  keycloak.realm(config.getRealm()).roles();
    }

    public List<String> getFilteredUserRoles(String userId) {
        RealmResource realmResource = getRealmResource();

        List<RoleRepresentation> effectiveRoles = realmResource.users()
                .get(userId)
                .roles()
                .realmLevel()
                .listEffective();

        return effectiveRoles.stream()
                .map(RoleRepresentation::getName)
                .filter(UserType::contains)
                .toList();
    }

    public RealmResource getRealmResource(){
     return  keycloak.realm(config.getRealm());
    }

}
