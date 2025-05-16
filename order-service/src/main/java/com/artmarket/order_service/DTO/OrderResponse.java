package com.artmarket.order_service.DTO;

import com.artmarket.order_service.DTO.client.PaintingResponse;
import com.artmarket.order_service.model.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for {@link com.artmarket.order_service.model.Order}
 */
public record OrderResponse(
        Long id,
        String userId,
        OrderStatus status,
        BigDecimal totalPrice,
        Instant createdAt,
        List<PaintingResponse> paintings,
        ShippingResponse shipping
) implements Serializable {
}