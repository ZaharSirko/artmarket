package com.artmarket.logistics_service.DTO;

public record CreateCounterpartyRequest(
        String FirstName,
        String MiddleName,
        String LastName,
        String Phone,
        String Email
) {}
