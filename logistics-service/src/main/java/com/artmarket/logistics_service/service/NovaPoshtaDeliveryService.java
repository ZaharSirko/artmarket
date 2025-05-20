package com.artmarket.logistics_service.service;

import com.artmarket.logistics_service.DTO.NovaPoshtaRequest;
import com.artmarket.logistics_service.DTO.NovaPoshtaResponse;
import com.artmarket.logistics_service.DTO.client.OrderResponse;
import com.artmarket.logistics_service.DTO.client.ShippingResponse;
import com.artmarket.logistics_service.DTO.client.UpdateShippingRequest;
import com.artmarket.logistics_service.client.NovaPoshtaClient;
import com.artmarket.logistics_service.client.OrderClient;
import com.artmarket.logistics_service.config.NovaPoshtaProperties;

import com.artmarket.logistics_service.event.DeliveryErrorEvent;
import com.artmarket.logistics_service.event.DeliveryStatusUpdateEvent;
import com.artmarket.logistics_service.service.hepler.DeliveryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovaPoshtaDeliveryService {
    private static final String DOCUMENT_TYPE = "InternetDocument";
    private static final String METHOD_NAME = "save";
    private static final String DEFAULT_WEIGHT = "1.0";
    private static final String SERVICE_TYPE = "WarehouseWarehouse";
    private static final String PAYMENT_METHOD = "Cash";
    private static final String CARGO_TYPE = "Cargo";

    private final OrderClient orderClient;
    private final NovaPoshtaClient novaPoshtaClient;
    private final NovaPoshtaProperties novaPoshtaProperties;
    private final DeliveryValidator deliveryValidator;
    private final KafkaDeliveryEventProducer kafkaProducer;

    public void processOrderDelivery(Long orderId) {
        try {
            OrderResponse order = fetchAndValidateOrder(orderId);

            kafkaProducer.send(
                    new DeliveryStatusUpdateEvent(
                            orderId,
                            "PROCESSING",
                            null,
                            LocalDateTime.now()
                    )
            );

            NovaPoshtaRequest npRequest = buildNovaPoshtaRequest(order);
            NovaPoshtaResponse npResponse = createNovaPoshtaDelivery(npRequest, orderId);
            String trackingNumber = extractTrackingNumber(npResponse);

            updateOrderShipping(orderId, trackingNumber);

            // Відправляємо подію про успішне створення
            kafkaProducer.send(
                    new DeliveryStatusUpdateEvent(
                            orderId,
                            "CREATED",
                            trackingNumber,
                            LocalDateTime.now()
                    )
            );

            log.info("Processed delivery for order {}, tracking: {}", orderId, trackingNumber);

        } catch (Exception e) {
            log.error("Delivery processing failed for order {}: {}", orderId, e.getMessage());

            kafkaProducer.send(
                    new DeliveryErrorEvent(
                            orderId,
                            e.getMessage(),
                            LocalDateTime.now()
                    )
            );

            throw new RuntimeException ("Failed to process delivery", e);
        }
    }

    private OrderResponse fetchAndValidateOrder(Long orderId) {
        OrderResponse order = orderClient.getOrderById(orderId);
        deliveryValidator.validateOrderForDelivery(order);
        return order;
    }

    private NovaPoshtaResponse createNovaPoshtaDelivery(NovaPoshtaRequest request, Long orderId) {
        try {
            NovaPoshtaResponse response = novaPoshtaClient.createDelivery(request);

            if (!response.success()) {
                String errorMessage = response.errors() != null ?
                        String.join(", ", response.errors()) : "Unknown error";
                throw new RuntimeException ("Nova Poshta API error: " + errorMessage);
            }

            if (response.data() == null || response.data().isEmpty()) {
                throw new RuntimeException ("Empty response data from Nova Poshta");
            }

            return response;

        } catch (Exception e) {
            kafkaProducer.send(
                    new DeliveryErrorEvent(
                            orderId,
                            "NP_API_ERROR: " + e.getMessage(),
                            LocalDateTime.now()
                    )
            );
            throw e;
        }
    }

    private void updateOrderShipping(Long orderId, String trackingNumber) {
        try {
            UpdateShippingRequest updateRequest = new UpdateShippingRequest(
                    "NovaPoshta",
                    trackingNumber,
                    "SENT"
            );

            orderClient.updateShipping(orderId, updateRequest);

            kafkaProducer.send(
                    new DeliveryStatusUpdateEvent(
                            orderId,
                            "SHIPPING_UPDATED",
                            trackingNumber,
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {
            kafkaProducer.send(
                    new DeliveryErrorEvent(
                            orderId,
                            "UPDATE_FAILED: " + e.getMessage(),
                            LocalDateTime.now()
                    )
            );
            throw e;
        }
    }

    private NovaPoshtaRequest buildNovaPoshtaRequest(OrderResponse order) {
        ShippingResponse shipping = order.shipping();

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("CitySender", novaPoshtaProperties.senderCityRef());
        properties.put("WarehouseSender", novaPoshtaProperties.senderWarehouseRef());
        properties.put("CityRecipient", shipping.city());
        properties.put("WarehouseRecipient", shipping.warehouse());
        properties.put("Weight", calculateWeight(order));
        properties.put("ServiceType", SERVICE_TYPE);
        properties.put("RecipientName", shipping.recipientName());
        properties.put("RecipientPhone", normalizePhone(shipping.phone()));
        properties.put("PaymentMethod", PAYMENT_METHOD);
        properties.put("CargoType", CARGO_TYPE);
        properties.put("Cost", order.totalPrice());
        properties.put("SeatsAmount", 1);

        return new NovaPoshtaRequest(
                novaPoshtaProperties.apiKey(),
                DOCUMENT_TYPE,
                METHOD_NAME,
                properties
        );
    }

    private String extractTrackingNumber(NovaPoshtaResponse response) {
        try {
            Map<String, Object> firstResult = (Map<String, Object>) response.data().getFirst();
            return firstResult.get("IntDocNumber").toString();
        } catch (Exception e) {
            try {
                throw new Exception("Failed to extract tracking number from response", e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public OrderResponse getOrderDetails(Long orderId) {
        return orderClient.getOrderById(orderId);
    }

    private String calculateWeight(OrderResponse order) {
        return DEFAULT_WEIGHT;
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }
}
