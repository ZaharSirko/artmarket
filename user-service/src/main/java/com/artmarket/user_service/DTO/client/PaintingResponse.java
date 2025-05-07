package com.artmarket.user_service.DTO.client;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaintingResponse(
        Long id,
        String title,
        String imageURL,
        String author,
        BigDecimal price,
        String description,
        LocalDate releaseDate
) {}
