package com.artmarket.logistics_service.service.hepler;

import com.artmarket.logistics_service.DTO.client.OrderResponse;
import com.artmarket.logistics_service.DTO.client.ShippingResponse;
import com.artmarket.logistics_service.client.OrderClient;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class DeliveryValidator {
    private final OrderClient orderClient;

    public void validateOrderForDelivery(OrderResponse order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (order.shipping() == null) {
            throw new IllegalArgumentException("Order has no shipping information");
        }

        ShippingResponse shipping = order.shipping();

        if (StringUtils.isBlank(shipping.city())) {
            throw new IllegalArgumentException("Recipient city is required");
        }

        if (StringUtils.isBlank(shipping.warehouse())) {
            throw new IllegalArgumentException("Recipient warehouse is required");
        }

        if (StringUtils.isBlank(shipping.recipientFullName())) {
            throw new IllegalArgumentException("Recipient name is required");
        }

        if (StringUtils.isBlank(shipping.phone())) {
            throw new IllegalArgumentException("Recipient phone is required");
        }

    }
}
