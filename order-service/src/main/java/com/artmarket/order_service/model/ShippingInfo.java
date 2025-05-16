package com.artmarket.order_service.model;

import com.artmarket.order_service.model.enums.ShippingStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo {

    private String shippingProvider; // e.g. "NOVA_POSHTA"
    private String trackingNumber;

    private String recipientName;
    private String phone;

    private String city;
    private String warehouse;

    @Enumerated(EnumType.STRING)
    private ShippingStatus shippingStatus;
}

