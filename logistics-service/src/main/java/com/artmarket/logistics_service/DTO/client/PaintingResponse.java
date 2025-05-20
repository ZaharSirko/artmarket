package com.artmarket.logistics_service.DTO.client;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public record PaintingResponse(
        Long id,
        String title,
        String description,
        String author,
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date releaseDate,
        Double price,
        String imageULR,
        String userId
) {}