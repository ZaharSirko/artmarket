package com.artmarket.order_service.client;


import com.artmarket.order_service.DTO.client.PaintingResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;


public interface PaintingClient {
    Logger log = LoggerFactory.getLogger(PaintingClient.class);

    @GetExchange("/paintings/by-ids}")
    @CircuitBreaker(name = "paintingService", fallbackMethod = "fallbackPaintingsByIds")
    @Retry(name = "paintingService")
    List<PaintingResponse> getPaintingsByIds(@RequestParam List<Long> ids);

    @GetExchange("/paintings/{id}")
    @CircuitBreaker(name = "paintingService", fallbackMethod = "fallbackPaintingById")
    @Retry(name = "paintingService")
    PaintingResponse getPaintingById(@PathVariable Long id);

    default  List<PaintingResponse> fallbackPaintingsByIds(Throwable throwable) {
        log.error("Cannot fetch painting info: {}", throwable.getMessage());
        throw new IllegalStateException("Painting service not available");
    }

    default  PaintingResponse fallbackPaintingById(Throwable throwable) {
        log.error("Cannot fetch painting info: {}", throwable.getMessage());
        throw new IllegalStateException("Painting service not available");
    }
}
