package com.artmarket.logistics_service.client;


import com.artmarket.logistics_service.DTO.NovaPoshtaRequest;
import com.artmarket.logistics_service.DTO.NovaPoshtaResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface NovaPoshtaClient {
    Logger log = LoggerFactory.getLogger(NovaPoshtaClient.class);

    @PostExchange("/")
    @CircuitBreaker(name = "novaPoshtaService", fallbackMethod = "fallbackNovaPoshta")
    @Retry(name = "novaPoshtaService")
    NovaPoshtaResponse post(@RequestBody NovaPoshtaRequest request);

    default NovaPoshtaResponse fallbackNovaPoshta(NovaPoshtaRequest request, Throwable throwable) {
        log.error("Nova Poshta service unavailable: {}", throwable.getMessage());
        throw new IllegalStateException("Nova Poshta service not available", throwable);
    }
}


