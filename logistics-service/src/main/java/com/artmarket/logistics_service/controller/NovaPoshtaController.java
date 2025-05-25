package com.artmarket.logistics_service.controller;

import com.artmarket.logistics_service.DTO.*;
import com.artmarket.logistics_service.service.NovaPoshtaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nova-poshta")
@RequiredArgsConstructor
public class NovaPoshtaController {
    private static final String DELIVERIES = "/deliveries";
    private static final String CITIES = "/cities";
    private static final String WAREHOUSES = "/warehouses";
    private static final String TRACKING_STATUS = DELIVERIES+"/{trackingNumber}/status";


    private final NovaPoshtaService novaPoshtaService;

    @PostMapping(DELIVERIES)
    public ResponseEntity<DocumentResponse> createDelivery(
            @RequestBody @Valid NovaPoshtaDeliveryRequest request) {
        DocumentResponse response = novaPoshtaService.createDelivery(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(CITIES)
    public ResponseEntity<List<Map<String, Object>>> getCities(
            @RequestParam String cityName) {
        List<Map<String, Object>> cities = novaPoshtaService.getCities(cityName);
        return ResponseEntity.ok(cities);
    }

    @GetMapping(WAREHOUSES)
    public ResponseEntity<List<Map<String, Object>>> getWarehouses(
            @RequestParam String cityName) {
        List<Map<String, Object>> warehouses = novaPoshtaService.getWarehouses(cityName);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping(TRACKING_STATUS)
    public ResponseEntity<String> getDeliveryStatus(
            @PathVariable String trackingNumber) {
        String status = novaPoshtaService.getDocumentStatus(trackingNumber);
        return ResponseEntity.ok(status);
    }
}