package com.artmarket.order_service.service;


import com.artmarket.events.OrderUpdatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventConsumer {
    private final OrderService orderService;
    private final MeterRegistry meterRegistry;

    @KafkaListener(topics = "#{'${kafka.topics.order-updated}'}")
    public void handleOrderUpdated(@Payload OrderUpdatedEvent event) {
        try {
            log.debug("Processing order update event: {}", event);
            meterRegistry.counter("order.update.events.received").increment();

            orderService.updateOrderFromEvent(event);

            meterRegistry.counter("order.update.events.processed").increment();
        } catch (Exception e) {
            log.error("Failed to process order update event {}: {}", event.orderId(), e.getMessage());
            meterRegistry.counter("order.update.events.failed").increment();}
    }
}