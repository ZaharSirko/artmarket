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
    private static final String UPDATE_BY_ORDER_ID = "/{orderId}/add-painting";
    private static final String DELETE_BY_ORDER_ID = "/{orderId}/delete-painting";
    private final OrderService orderService;

    @GetMapping(BY_ID)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
         var order = orderService.getOrderById(id);
         return  ResponseEntity.ok(order);
    }

    @PostMapping()
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest,
                                                     @AuthenticationPrincipal Jwt jwt) {
        var response = orderService.createOrder(orderRequest, jwt.getTokenValue());
        return ResponseEntity.ok(response);
    }

    @GetMapping(MY_ORDER)
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt) {
        List<OrderResponse> orders = orderService.getOrdersForCurrentUser(jwt.getTokenValue());
        return ResponseEntity.ok(orders);
    }

    @PutMapping(UPDATE_BY_ORDER_ID)
    public ResponseEntity<OrderResponse> addPaintingFromOrder(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderService.addPaintingToOrder(orderId, request));
    }

    @PutMapping(DELETE_BY_ORDER_ID)
    public ResponseEntity<OrderResponse> removePaintingFromOrder(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderService.removePaintingFromOrder(orderId, request));
    }

}
