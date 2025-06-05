package com.artmarket.user_service.DTO.client;

import com.artmarket.dto.PaintingResponse;

import java.util.List;

public record PageResponse(
        List<PaintingResponse> content,
        int totalPages,
        int totalElements,
        int number,
        int size,
        boolean first,
        boolean last
) {}