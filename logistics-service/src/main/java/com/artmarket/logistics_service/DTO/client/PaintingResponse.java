package com.artmarket.logistics_service.DTO.client;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

public record PaintingResponse(
        Long id,
        String title,
        String description,
        String author,
        @DateTimeFormat(pattern = "yyyy-MM-dd")  Date releaseDate,
        BigDecimal price,
        BigDecimal weight,
        BigDecimal width,
        BigDecimal height,
        BigDecimal depth,
        String imageULR,
        String userId) {
}