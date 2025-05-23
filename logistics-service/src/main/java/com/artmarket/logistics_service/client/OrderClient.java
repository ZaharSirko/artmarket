package com.artmarket.logistics_service.client;

import com.artmarket.logistics_service.DTO.client.OrderResponse;
import com.artmarket.logistics_service.DTO.client.UpdateShippingRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface OrderClient {
    Logger log = LoggerFactory.getLogger(OrderClient.class);

    @GetExchange("/orders/{orderId}")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetOrder")
    @Retry(name = "orderService")
    OrderResponse getOrderById(@PathVariable Long orderId);


    @PutExchange("/orders/{id}/shipping")
    void updateShipping(@PathVariable Long id, @RequestBody UpdateShippingRequest request);

    default OrderResponse fallbackGetOrder(Long orderId, Throwable throwable) {
        log.error("OrderService unavailable, failed to get order {}: {}", orderId, throwable.getMessage());
        throw new IllegalStateException("Order service is not available");
    }
}

