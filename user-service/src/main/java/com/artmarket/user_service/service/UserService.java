package com.artmarket.user_service.service;

import com.artmarket.user_service.DTO.OauthUserRequest;
import com.artmarket.user_service.DTO.UserCreateRequest;
import com.artmarket.user_service.DTO.UserResponse;
import com.artmarket.user_service.DTO.client.PaintingResponse;
import com.artmarket.user_service.DTO.client.UserWithPaintingsResponse;
import com.artmarket.user_service.client.PaintingClient;
import com.artmarket.user_service.model.User;
import com.artmarket.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final KeycloakUserService keycloakService;
    private final PaintingClient paintingClient;

    public UserResponse getUserById(Long id) {
     return   userRepository.findById(id)
                .map(user -> new UserResponse(
                 user.getId(),
                        user.getEmail(),
                        user.getKeycloakId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getType(),
                        user.getCreatedAt()
                )).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }


    @Transactional
    public void registerUser(UserCreateRequest request) {
     try {

         if (userRepository.existsByEmail(request.email())) {
             throw new Exception(request.email());
         }

         String keycloakId = keycloakService.createKeycloakUser(request);

         User user = User.builder()
                 .keycloakId(keycloakId)
                 .email(request.email())
                 .firstName(request.firstName())
                 .lastName(request.lastName())
                 .build();
         userRepository.save(user);
         log.info("User created");

     }catch (Exception e){
         log.error(e.getMessage());
     }
    }

    @Transactional
    public void handleOAuthUser(OauthUserRequest request){
        try {

            if (userRepository.existsByEmail(request.email())) {
                throw new Exception(request.email());
            }
            String keycloakId = keycloakService.createOAuthUser(request);
            User user = User.builder()
                    .keycloakId(keycloakId)
                    .email(request.email())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .build();
            userRepository.save(user);
            log.info("Oauth user created");

        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public UserResponse findByKeycloakId(String keycloakId) {
       return userRepository.findByKeycloakId(keycloakId)
               .map(user -> new UserResponse(
                       user.getId(),
                       user.getKeycloakId(),
                       user.getEmail(),
                       user.getFirstName(),
                       user.getLastName(),
                       user.getType(),
                       user.getCreatedAt()
               ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    public UserWithPaintingsResponse getUserWithPaintings(Long userId) {
        List<PaintingResponse> paintings = paintingClient.getUserPaintings(userId);
        return userRepository.findById(userId)
                .map(user -> new UserWithPaintingsResponse(
                        user.getId(),
                        user.getKeycloakId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getType(),
                        user.getCreatedAt(),
                        paintings
                ))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
