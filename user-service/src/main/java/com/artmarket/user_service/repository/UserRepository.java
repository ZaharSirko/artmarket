package com.artmarket.user_service.repository;

import com.artmarket.user_service.model.User;
import com.artmarket.user_service.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakId(String keycloakId);

    @Query("SELECT u FROM User u WHERE u.type = :type")
    Optional<User> findByType(UserType type);

    boolean existsByEmail(String email);
}
