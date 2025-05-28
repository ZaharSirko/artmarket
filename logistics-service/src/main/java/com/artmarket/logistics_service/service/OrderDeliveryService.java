package com.artmarket.logistics_service.service;

import com.artmarket.DTO.*;
import com.artmarket.config.KafkaTopics;
import com.artmarket.events.OrderUpdatedEvent;
import com.artmarket.logistics_service.DTO.DeliveryRequest;
import com.artmarket.logistics_service.DTO.NovaPoshtaDeliveryRequest;
import com.artmarket.logistics_service.DTO.DocumentResponse;
import com.artmarket.logistics_service.client.OrderClient;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDeliveryService {
    private final NovaPoshtaService novaPoshtaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopics kafkaTopics;
    private final OrderClient orderClient;

    @Transactional
    public DocumentResponse processOrderDelivery(Long orderId,String authToken, DeliveryRequest request) {
        log.info("[Order {}] Starting delivery processing", orderId);

        try {
            log.debug("[Order {}] Fetching order details from order service", orderId);
            OrderResponse order = orderClient.getOrder(orderId, authToken);
            log.info("[Order {}] Successfully retrieved order with {} paintings", orderId, order.paintings().size());

            NovaPoshtaDeliveryRequest enrichedRequest = enrichDeliveryRequest(request, order);
            DocumentResponse documentResponse = novaPoshtaService.createDelivery(enrichedRequest);

            OrderUpdateRequest updateRequest = createOrderUpdateRequest(documentResponse, enrichedRequest, order);
            sendOrderUpdateEvent(orderId,order.userId(), updateRequest);

            log.info("[Order {}] Delivery processing completed successfully", orderId);
            return documentResponse;

        } catch (Exception e) {
            log.error("[Order {}] Error processing delivery: {}", orderId, e.getMessage(), e);
            sendDeliveryFailedEvent(orderId, e.getMessage());
            throw new DeliveryProcessingException("Failed to process delivery for order " + orderId, e);
        }
    }

    private void sendOrderUpdateEvent(Long orderId,String userId, OrderUpdateRequest updateRequest) {
        OrderUpdatedEvent event = new OrderUpdatedEvent(
                orderId,
                userId,
                updateRequest.deliveryPrice(),
                updateRequest.totalPrice(),
                updateRequest.shipping(),
                LocalDateTime.now()
        );

        kafkaTemplate.send(kafkaTopics.getOrderUpdated(), event);
        log.info("Sent OrderUpdatedEvent for order {}", orderId);
    }

    private void sendDeliveryFailedEvent(Long orderId, String errorMessage) {
        Map<String, Object> event = Map.of(
                "orderId", orderId,
                "error", errorMessage,
                "timestamp", LocalDateTime.now()
        );
        kafkaTemplate.send(kafkaTopics.getShippingFailed(), event);
    }

    private NovaPoshtaDeliveryRequest enrichDeliveryRequest(DeliveryRequest request, OrderResponse order) {
        log.debug("Enriching delivery request with paintings information");

        String description = order.paintings().stream()
                .map(p -> String.format("%s (ID: %d, author: %s)", p.title(), p.id(), p.author()))
                .collect(Collectors.joining(", "));
        log.trace("Generated description: {}", description);

        String volume = order.paintings().stream()
                .map(p -> p.width()
                        .multiply(p.height())
                        .multiply(p.depth())
                        .divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .toString();
        log.debug("Calculated total volume: {} m³", volume);

        String weight = String.valueOf(order.paintings().stream()
                .map(PaintingResponse::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        log.debug("Calculated total weight: {} kg", weight);

        String cost = String.valueOf(order.itemsPrice());

        return new NovaPoshtaDeliveryRequest(
                request.cityName(),
                request.warehouseNumber(),
                request.recipientFirstName(),
                request.recipientMiddleName(),
                request.recipientLastName(),
                request.recipientPhone(),
                request.recipientEmail(),
                description,
                cost,
                weight,
                volume
        );
    }

    private OrderUpdateRequest createOrderUpdateRequest(DocumentResponse documentResponse,
                                                        NovaPoshtaDeliveryRequest deliveryRequest,
                                                        OrderResponse order) {
        log.debug("Creating order update request from delivery response");

        BigDecimal deliveryPrice = BigDecimal.valueOf(documentResponse.CostOnSite());
        log.debug("Delivery cost: {}", deliveryPrice);

        BigDecimal totalPrice = order.itemsPrice().add(deliveryPrice);
        log.debug("Calculated total price: {} (items: {} + delivery: {})",
                totalPrice, order.itemsPrice(), deliveryPrice);

        String fullName = String.join(" ",
                deliveryRequest.recipientFirstName(),
                deliveryRequest.recipientMiddleName(),
                deliveryRequest.recipientLastName());
        log.trace("Recipient full name: {}", fullName);

        ShippingUpdate shippingUpdate = new ShippingUpdate(
                "NovaPoshta",
                documentResponse.IntDocNumber(),
                deliveryRequest.cityName(),
                deliveryRequest.warehouseNumber(),
                fullName,
                deliveryRequest.recipientPhone(),
                deliveryRequest.recipientEmail(),
                ShippingStatus.REGISTERED
        );

        return new OrderUpdateRequest(
                deliveryPrice,
                totalPrice,
                shippingUpdate
        );
    }

    public static class DeliveryProcessingException extends RuntimeException {
        public DeliveryProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}