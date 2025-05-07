package com.artmarket.user_service.service;

import com.artmarket.user_service.DTO.OauthUserRequest;
import com.artmarket.user_service.DTO.UserCreateRequest;
import com.artmarket.user_service.config.KeycloakConfig;
import com.artmarket.user_service.service.helpers.KeycloakHelper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {
    private static final Logger log = LoggerFactory.getLogger(KeycloakUserService.class);
    private final KeycloakHelper keycloakHelper;
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Transactional
    public String createKeycloakUser(UserCreateRequest request) {
        String userId = "";
        try {
         var userResponse = keycloakHelper.createUser(request);
         log.info("Keycloak user created: {}", userResponse);

         userId = keycloakHelper.extractUserId(userResponse);
         log.info("Get keycloak user id: {}", userId);

         keycloakHelper.setUserPassword(userId, request.password());
         log.info("Password set for keycloak user: {}", userId);

         keycloakHelper.assignRole(userId, "USER");
         log.info("Assigned roles: {}", "USER");

     }catch (Exception e) {
         log.error("Keycloak user creation failed", e);
     }
        log.info("Keycloak user created: {}", userId);
        return userId;
    }


    String createOAuthUser(OauthUserRequest request){
        String userId = "";
        try {
            var userResponse = keycloakHelper.createUser(request);
            log.info("Keycloak user created: {}", userResponse);

            userId = keycloakHelper.extractUserId(userResponse);
            log.info("Get keycloak user id: {}", userId);

            var password = encoder().encode(UUID.randomUUID().toString());
            keycloakHelper.setUserPassword(userId,password);
            log.info("Password set for keycloak user: {}", userId);

            keycloakHelper.assignRole(userId, "USER");
            log.info("Assigned roles: {}", "USER");

        }catch (Exception e) {
            log.error("Keycloak user creation failed", e);
        }
        log.info("Keycloak user created: {}", userId);
        return userId;
    }

}

