package com.artmarket.logistics_service.service;

import com.artmarket.logistics_service.event.DeliveryErrorEvent;
import com.artmarket.logistics_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final NovaPoshtaDeliveryService deliveryService;
    private final KafkaDeliveryEventProducer eventProducer;

    @KafkaListener(topics = "${kafka.topics.order-created}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order {}", event.orderId());
        try {
            deliveryService.processOrderDelivery(event.orderId());
            log.info("Successfully processed delivery for order {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process delivery for order {}: {}", event.orderId(), e.getMessage());
            eventProducer.send(
                    new DeliveryErrorEvent(
                            event.orderId(),
                            "DELIVERY_PROCESSING_FAILED: " + e.getMessage(),
                            LocalDateTime.now()
                    )
            );
        }
    }
}
