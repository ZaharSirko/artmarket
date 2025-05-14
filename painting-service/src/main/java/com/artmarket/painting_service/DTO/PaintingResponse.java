package com.artmarket.painting_service.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO for {@link com.artmarket.painting_service.model.Painting}
 */
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