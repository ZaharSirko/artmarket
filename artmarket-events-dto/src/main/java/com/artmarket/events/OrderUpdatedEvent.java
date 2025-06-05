package com.artmarket.events;


import com.artmarket.dto.ShippingUpdate;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record OrderUpdatedEvent(
        Long orderId,
        String userId,
        BigDecimal deliveryPrice,
        BigDecimal totalPrice,
        ShippingUpdate shipping,
        LocalDateTime eventTime
) implements BaseEvent {}