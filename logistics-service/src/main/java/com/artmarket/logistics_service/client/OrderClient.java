package com.artmarket.logistics_service.client;

import com.artmarket.logistics_service.DTO.client.OrderResponse;
import com.artmarket.logistics_service.DTO.client.OrderUpdateRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface OrderClient {
    Logger log = LoggerFactory.getLogger(OrderClient.class);

    @GetExchange("/orders/{orderId}")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetOrder")
    OrderResponse getOrder(@PathVariable Long orderId, @RequestHeader("Authorization") String token);

    @PutExchange("/orders/{orderId}")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackUpdateOrder")
    void updateShipping(@PathVariable Long orderId,
                     @RequestHeader("Authorization") String token,
                     @RequestBody OrderUpdateRequest request);

    default OrderResponse fallbackGetOrder(Long id, String token, Throwable throwable) {
        log.error("Failed to get order {}: {}", id, throwable.getMessage());
        throw new IllegalStateException("Order service unavailable", throwable);
    }

    default void fallbackUpdateOrder(Long id, String token, OrderUpdateRequest request, Throwable throwable) {
        log.error("Failed to update order {}: {}", id, throwable.getMessage());
        throw new IllegalStateException("Order update failed", throwable);
    }
}


