package com.artmarket.order_service.service.heplers;

import com.artmarket.order_service.DTO.OrderRequest;
import com.artmarket.order_service.client.PaintingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderValidator {
    private final PaintingClient paintingClient;

    public void validateOrderRequest(OrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

//        if (request.shipping() == null) {
//            throw new IllegalArgumentException("Shipping information is required");
//        }

    }

    public void validatePaintingAvailable(Long paintingId) {
//        if (!paintingClient.isPaintingAvailable(paintingId)) {
//            throw new IllegalStateException("Painting is not available");
//        }
    }
}
