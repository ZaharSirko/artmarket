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
    private static final String ROOT = "";
    private static final String ID_PATH = "/{id}";
    private static final String MY_ORDERS = "/my";
    private static final String ORDER_ITEMS = "/{orderId}/items";
    private static final String ORDER_SHIPPING = "/{orderId}/shipping";

    private final OrderService orderService;

    @GetMapping(ID_PATH)
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getOrderById(id, jwt.getTokenValue()));
    }

    @PostMapping(ROOT)
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest, jwt.getTokenValue()));
    }

    @GetMapping(MY_ORDERS)
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getOrdersForCurrentUser(jwt.getTokenValue()));
    }

    @PutMapping(ORDER_ITEMS)
    public ResponseEntity<OrderResponse> addPaintingToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderService.addPaintingToOrder(orderId, request, jwt.getTokenValue()));
    }

    @DeleteMapping(ORDER_ITEMS)
    public ResponseEntity<OrderResponse> removePaintingFromOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderService.removePaintingFromOrder(orderId, request, jwt.getTokenValue()));
    }

    @PatchMapping(ORDER_SHIPPING)
    public ResponseEntity<OrderResponse> updateShipping(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateShippingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderService.updateShipping(orderId, request, jwt.getTokenValue()));
    }
}

