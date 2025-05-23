package com.artmarket.logistics_service.DTO;

import com.artmarket.logistics_service.config.NovaPoshtaProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public record DeliveryDocumentRequest(
        String CityRecipient,
        String Recipient,
        String RecipientAddress,
        String ContactRecipient,
        String RecipientsPhone,
        String Description,
        String Weight,
        String VolumeGeneral
) {
    public Map<String, Object> toMethodProperties(NovaPoshtaProperties props) {
        return Map.ofEntries(
                Map.entry("PayerType", "Recipient"),
                Map.entry("PaymentMethod", "Cash"),
                Map.entry("DateTime", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                Map.entry("CargoType", "Parcel"),
                Map.entry("VolumeGeneral", VolumeGeneral),
                Map.entry("Weight", Weight),
                Map.entry("ServiceType", "WarehouseWarehouse"),
                Map.entry("SeatsAmount", "1"),
                Map.entry("Description", Description),

                // Дані відправника з NovaPoshtaProperties
                Map.entry("CitySender", props.citySender()),
                Map.entry("Sender", props.sender()),
                Map.entry("SenderAddress", props.senderAddress()),
                Map.entry("ContactSender", props.contactSender()),
                Map.entry("SendersPhone", props.senderPhone()),

                // Дані отримувача
                Map.entry("CityRecipient", CityRecipient),
                Map.entry("Recipient", Recipient),
                Map.entry("RecipientAddress", RecipientAddress),
                Map.entry("ContactRecipient", ContactRecipient),
                Map.entry("RecipientsPhone", RecipientsPhone)
        );
    }
}
