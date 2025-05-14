package com.artmarket.user_service.DTO.client;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public record PageResponse(
        List<PaintingResponse> content,
        int totalPages,
        int totalElements,
        int number,
        int size,
        boolean first,
        boolean last
) {}