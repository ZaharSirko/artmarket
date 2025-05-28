package com.artmarket.notification_service.consumer;

import com.artmarket.events.OrderEvent;
import com.artmarket.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;

    @KafkaListener(topics = "#{'${kafka.topics.orders}'}", groupId = "notification-group")
    public void listen(OrderEvent event) {
        log.info("Received order event: {}", event);
        emailService.sendOrderConfirmation(event);
    }
}
