package com.artmarket.order_service.model;

import com.artmarket.order_service.model.enums.ShippingStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Nullable
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingInfo {

    String shippingProvider;
    String trackingNumber;

    String city;
    String warehouse;

    String recipientFullName;
    String phone;
    String email;

    @Enumerated(EnumType.STRING)
    ShippingStatus shippingStatus;
}

