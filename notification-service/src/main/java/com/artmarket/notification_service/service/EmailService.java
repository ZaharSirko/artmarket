package com.artmarket.notification_service.service;

import com.artmarket.DTO.PaintingResponse;
import com.artmarket.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
 private final JavaMailSender mailSender;

    public void sendOrderConfirmation(OrderEvent orderEvent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(orderEvent.userId());
        message.setSubject("Your Order #" + orderEvent.orderId() + " is Confirmed!");
        message.setText(buildBody(orderEvent));

        mailSender.send(message);
        log.info("Email sent for order {}", orderEvent.orderId());
    }

    private String buildBody(OrderEvent event) {
        StringBuilder paintingsInfo = new StringBuilder();
        for (PaintingResponse painting : event.paintings()) {
            paintingsInfo.append("🎨 Title: ").append(painting.title()).append("\n")
                    .append("📝 Description: ").append(painting.description()).append("\n\n");
        }

        return """
            Hello, %s!

            ✅ Your order has been successfully processed.

            📦 Tracking Number: %s
            💰 Total Price: %s UAH
            🚚 Delivery method: %s
            🏬 Delivery to: %s, warehouse #%s
            🎨 Paintings in order: %d

            %s

            📅 Order Date: %s

            If you have questions, contact us at support@artmarket.com.

            Thank you for choosing ArtMarket!
            """.formatted(
                event.shipping().recipientFullName(),
                event.shipping().trackingNumber(),
                event.totalPrice(),
                event.shipping().shippingProvider(),
                event.shipping().city(),
                event.shipping().warehouse(),
                event.paintings().size(),
                paintingsInfo.toString(),
                event.eventTime()
        );
    }

}