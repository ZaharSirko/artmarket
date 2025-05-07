package com.artmarket.painting_service.client;

import com.artmarket.painting_service.DTO.client.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

public interface UserClient {
    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetExchange("/users/me")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
    @Retry(name = "userService")
    UserResponse getCurrentUser(@RequestHeader("Authorization") String bearerToken);

    default UserResponse fallbackUser(String bearerToken, Throwable throwable) {
        log.error("Cannot fetch user info: {}", throwable.getMessage());
        throw new IllegalStateException("User service not available");
    }
}

