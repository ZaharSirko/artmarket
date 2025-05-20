package com.artmarket.order_service.controller;

import com.artmarket.order_service.DTO.OrderItemRequest;
import com.artmarket.order_service.DTO.OrderRequest;
import com.artmarket.order_service.DTO.OrderResponse;
import com.artmarket.order_service.DTO.UpdateShippingRequest;
import com.artmarket.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getOrderById(id, jwt.getTokenValue()));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest, jwt.getTokenValue()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getOrdersForCurrentUser(jwt.getTokenValue()));
    }

    @PutMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addPaintingToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderService.addPaintingToOrder(orderId, request, jwt.getTokenValue()));
    }

    @DeleteMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> removePaintingFromOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderService.removePaintingFromOrder(orderId, request, jwt.getTokenValue()));
    }

    @PatchMapping("/{orderId}/shipping")
    public ResponseEntity<OrderResponse> updateShipping(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateShippingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderService.updateShipping(orderId, request, jwt.getTokenValue()));
    }
}

