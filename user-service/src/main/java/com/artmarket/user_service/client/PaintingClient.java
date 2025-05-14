package com.artmarket.user_service.client;

import com.artmarket.user_service.DTO.client.PageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface PaintingClient {
    Logger log = LoggerFactory.getLogger(PaintingClient.class);

    @GetExchange("/paintings/user/{userId}")
    @CircuitBreaker(name = "paintingService", fallbackMethod = "fallbackUser")
    @Retry(name = "paintingService")
    PageResponse getUserPaintings(@PathVariable String userId);

    default PageResponse fallbackUser(Throwable throwable) {
        log.error("Cannot fetch painting info: {}", throwable.getMessage());
        throw new IllegalStateException("Painting service not available");
    }
}
