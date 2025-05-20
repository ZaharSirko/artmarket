package com.artmarket.logistics_service.controller;

import com.artmarket.logistics_service.DTO.client.OrderResponse;
import com.artmarket.logistics_service.service.NovaPoshtaDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class NovaPoshtaDeliveryController {

    private final NovaPoshtaDeliveryService deliveryService;

    @PostMapping("/{orderId}/process")
    public ResponseEntity<String> createDelivery(@PathVariable Long orderId) {
        try {
            deliveryService.processOrderDelivery(orderId);
            return ResponseEntity.ok("Delivery to order " + orderId + " created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getDeliveryStatus(@PathVariable Long orderId) {
        try {
            OrderResponse order = deliveryService.getOrderDetails(orderId);
            return ResponseEntity.ok(Map.of(
                    "status", order.shipping().shippingStatus(),
                    "trackingNumber", order.shipping().trackingNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}