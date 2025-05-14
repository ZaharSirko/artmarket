package com.artmarket.user_service.controller;


import com.artmarket.user_service.DTO.UserRegistrationRequest;
import com.artmarket.user_service.DTO.UserResponse;
import com.artmarket.user_service.DTO.client.PageResponse;
import com.artmarket.user_service.DTO.client.PaintingResponse;
import com.artmarket.user_service.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController()
@RequestMapping("/users")
public class UserController {
    private static final  String ME_USER_PAGE = "/me";
    private static  final String USER_PAGE = "/{userId}";
    private static  final String USER_PAINTINGS="/{userId}/paintings";
    private static  final String USER_REGISTRATION = "/registration";
    private static  final String USER_REGISTER_OAUTH = "/oauth/callback";
    private static  final String USER_UPDATE_PASSWORD = "/updatePassword";
    private static  final String USER_ASSIGN_ROLE = "/assign-role/{userId}";
    private static  final String USER_EMAIL_VERIFICATION = "/{userId}/send-verify-email";
    private final KeycloakUserService keycloakUserService;


    @GetMapping(ME_USER_PAGE)
    public ResponseEntity<UserResponse> getUser(Principal principal) {
        var user =  keycloakUserService.getUserById(principal.getName());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(USER_PAINTINGS)
    public ResponseEntity<PageResponse> getUserPaintings(@PathVariable String userId) {
        PageResponse paintings = keycloakUserService.getUserWithPaintings(userId).paintings();
        return new ResponseEntity<>(paintings, HttpStatus.OK);
    }


    @PostMapping(USER_REGISTRATION)
    public ResponseEntity<UserRegistrationRequest> createUser(@RequestBody UserRegistrationRequest request) {
        var user = keycloakUserService.createUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


    @DeleteMapping(USER_PAGE)
    public ResponseEntity<String> deleteUserById(@PathVariable String userId) {
        keycloakUserService.deleteUserById(userId);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    }


    @PutMapping(USER_EMAIL_VERIFICATION)
    public ResponseEntity<String> sendVerificationEmail(@PathVariable String userId) {
        keycloakUserService.emailVerification(userId);
        return new ResponseEntity<>("Email verification successfully", HttpStatus.OK);
    }


    @PutMapping(USER_UPDATE_PASSWORD)
    public ResponseEntity<String> updatePassword(@RequestBody String request, Principal principal) {
        keycloakUserService.updatePassword(request,principal.getName());
        return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
    }

    @PutMapping(USER_ASSIGN_ROLE)
    public ResponseEntity<String> assignRoleToUser(
            @PathVariable String userId,
            @RequestParam  String roleName) {
        try {
            keycloakUserService.assignRole(userId, roleName);
            return ResponseEntity.ok("Role '" + roleName + "' assigned to user " + userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role: " + e.getMessage());
        } catch (jakarta.ws.rs.ForbiddenException e) {
            return ResponseEntity.status(403).body("Forbidden: insufficient permissions.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to assign role: " + e.getMessage());
        }
    }
}
