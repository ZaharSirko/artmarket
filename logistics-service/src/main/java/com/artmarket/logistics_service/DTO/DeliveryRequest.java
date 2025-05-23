package com.artmarket.logistics_service.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeliveryRequest(
        @NotBlank String cityName,
        @NotBlank String warehouseNumber,
        @NotBlank String recipientFirstName,
        String recipientMiddleName,
        @NotBlank String recipientLastName,
        @NotBlank @Pattern(regexp = "^380\\d{9}$") String recipientPhone,
        @Email String recipientEmail,
        @NotBlank String description,
        @NotBlank @DecimalMin("0.1") String weight,
        @NotBlank @DecimalMin("0.01") String volumeGeneral
) {}
