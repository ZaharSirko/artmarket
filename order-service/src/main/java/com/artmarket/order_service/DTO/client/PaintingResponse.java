package com.artmarket.order_service.DTO.client;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


public record PaintingResponse(
        Long id,
        String title,
        String description,
        String author,
        @DateTimeFormat(pattern = "yyyy-MM-dd")  Date releaseDate,
        BigDecimal price,
        String imageULR,
        String userId) implements Serializable {
}