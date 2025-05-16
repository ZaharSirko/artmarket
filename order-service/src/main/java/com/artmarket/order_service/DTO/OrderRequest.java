package com.artmarket.order_service.DTO;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull String userId,
        @NotNull List<OrderItemRequest> items,
        @NotNull ShippingRequest shipping
) {}
