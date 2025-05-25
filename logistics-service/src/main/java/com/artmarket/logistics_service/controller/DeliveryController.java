package com.artmarket.logistics_service.controller;

import com.artmarket.logistics_service.DTO.DeliveryRequest;
import com.artmarket.logistics_service.DTO.DocumentResponse;
import com.artmarket.logistics_service.service.OrderDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class DeliveryController {
    private static final String DELIVERIES = "/{orderId}/deliveries";
    private final OrderDeliveryService orderDeliveryService;

    @PostMapping(DELIVERIES)
    public ResponseEntity<DocumentResponse> createDelivery(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid DeliveryRequest request) {

        DocumentResponse response = orderDeliveryService.processOrderDelivery(orderId, token, request);
        return ResponseEntity.ok(response);
    }
}
