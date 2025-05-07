package com.artmarket.user_service.controller;

import com.artmarket.user_service.DTO.OauthUserRequest;
import com.artmarket.user_service.DTO.UserCreateRequest;
import com.artmarket.user_service.DTO.UserResponse;
import com.artmarket.user_service.DTO.client.PaintingResponse;
import com.artmarket.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController()
@RequestMapping("/users")
public class UserController {
    private static final  String ME_USER_PAGE = "/users/me";
    private static  final String USER_PAGE = "/{id}";
    private static  final String USER_PAINTINGS="/{id}/paintings";
    private static  final String USER_REGISTER = "/register";
    private static  final String USER_REGISTER_OAUTH = "/oauth/callback";
    private final UserService userService;


    @GetMapping(ME_USER_PAGE)
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt principal) {
        String keycloakId = principal.getSubject();
        UserResponse userResponse = userService.findByKeycloakId(keycloakId);
        return ResponseEntity.ok(userResponse);
    }


    @GetMapping(USER_PAINTINGS)
    public ResponseEntity<List<PaintingResponse>> getUserPaintings(@PathVariable Long id) {
        List<PaintingResponse> paintings = userService.getUserWithPaintings(id).paintings();
        return ResponseEntity.ok(paintings);
    }


    @GetMapping(USER_PAGE)
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
       var currentUser = userService.getUserById(id);
        return new ResponseEntity<>(currentUser, HttpStatus.OK);
    }

    @GetMapping(USER_REGISTER_OAUTH)
    public ResponseEntity<String> oauthCallback(@AuthenticationPrincipal Jwt principal) {
        String email = principal.getClaimAsString("email");
        String firstName = principal.getClaimAsString("given_name"); // або "firstName"
        String lastName = principal.getClaimAsString("family_name"); // або "lastName"

        OauthUserRequest request = new OauthUserRequest(email, firstName, lastName);
        userService.handleOAuthUser(request);

        return ResponseEntity.ok("User registered successfully via OAuth2");
    }


    @PostMapping(USER_REGISTER)
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserCreateRequest request) {
        userService.registerUser(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
