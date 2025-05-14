package com.artmarket.user_service.DTO.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public record PaintingResponse(
        Long id,
        String title,
        @JsonProperty("imageULR") String imageURL,
        String author,
        BigDecimal price,
        String description,
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date releaseDate
) {}
