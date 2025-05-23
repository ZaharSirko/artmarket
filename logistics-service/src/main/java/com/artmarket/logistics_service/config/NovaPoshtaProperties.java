package com.artmarket.logistics_service.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "novaposhta")
public record NovaPoshtaProperties(
        String apiKey,
        String apiUrl,
        String citySender,
        String sender,
        String senderAddress,
        String contactSender,
        String senderPhone
) {}