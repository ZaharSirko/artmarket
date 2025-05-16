package com.artmarket.order_service.DTO;

import org.antlr.v4.runtime.misc.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link com.artmarket.order_service.model.OrderItem}
 */
public record OrderItemRequest(
        @NotNull Long paintingId
) implements Serializable {
}