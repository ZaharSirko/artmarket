package com.artmarket.logistics_service.service;

import com.artmarket.logistics_service.config.KafkaTopics;
import com.artmarket.logistics_service.event.DeliveryErrorEvent;
import com.artmarket.logistics_service.event.DeliveryStatusUpdateEvent;
import com.artmarket.logistics_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaDeliveryEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    public void send(DeliveryStatusUpdateEvent event) {
        sendEvent(kafkaTopics.getDeliveryStatusUpdated(), event.orderId().toString(), event);
    }

    public void send(DeliveryErrorEvent event) {
        sendEvent(kafkaTopics.getDeliveryErrors(), event.orderId().toString(), event);
    }

    public void send(OrderCreatedEvent event) {
        sendEvent(kafkaTopics.getOrderCreated(), event.orderId().toString(), event);
    }

    private void sendEvent(String topic, String key, Object value) {
        try {
            kafkaTemplate.send(topic, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send event to {}: {}", topic, ex.getMessage());
                        } else {
                            log.debug("Event sent to {}: {}", topic, value);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending Kafka message: {}", e.getMessage());
        }
    }
}