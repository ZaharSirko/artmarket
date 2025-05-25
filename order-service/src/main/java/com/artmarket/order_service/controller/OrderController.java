package com.artmarket.order_service.controller;

import com.artmarket.order_service.DTO.*;
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
    private static final String ID_PATH = "/{orderId}";
    private static final String MY_ORDERS = "/my";
    private static final String ORDER_ITEMS = ID_PATH+"/items";

    private final OrderService orderService;

    @GetMapping(ID_PATH)
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, jwt.getTokenValue()));
    }


    @PutMapping(ID_PATH)
    public ResponseEntity<OrderResponse> updateOrderDelivery(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid OrderUpdateRequest request) {
        OrderResponse updatedOrder = orderService.updateOrder(orderId, request, jwt.getTokenValue());
        return ResponseEntity.ok(updatedOrder);
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

}

