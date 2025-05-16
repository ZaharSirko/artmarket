package com.artmarket.order_service.controller;

import com.artmarket.order_service.DTO.OrderItemRequest;
import com.artmarket.order_service.DTO.OrderRequest;
import com.artmarket.order_service.DTO.OrderResponse;
import com.artmarket.order_service.service.OrderService;
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
    private static final String BY_ID = "/{id}";
    private static final String MY_ORDER = "/my";
    private static final String BY_UPDATE_ORDER_ID = "/{orderId}/add-painting";
    private final OrderService orderService;

    @GetMapping(BY_ID)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
         var order = orderService.getOrderById(id);
         return  ResponseEntity.ok(order);
    }

    @PostMapping()
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        var response = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping(MY_ORDER)
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authHeader) {

        List<OrderResponse> orders = orderService.getOrdersForCurrentUser(authHeader);
        return ResponseEntity.ok(orders);
    }

    @PutMapping(BY_UPDATE_ORDER_ID)
    public ResponseEntity<OrderResponse> addPainting(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderService.addPaintingToOrder(orderId, request));
    }

}
