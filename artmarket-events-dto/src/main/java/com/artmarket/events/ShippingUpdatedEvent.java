package com.artmarket.events;


import com.artmarket.DTO.ShippingStatus;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public record ShippingUpdatedEvent(
        Long orderId,
        String userId,
        String provider,
        String trackingNumber,
        String city,
        String warehouse,
        String recipientFullName,
        String phone,
        String email,
        ShippingStatus shippingStatus,
        LocalDateTime eventTime
) implements BaseEvent {}

