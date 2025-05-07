package com.artmarket.user_service.client;

import com.artmarket.user_service.DTO.client.PaintingResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface PaintingClient {
    @GetExchange("/paintings/user/{userId}")
    List<PaintingResponse> getUserPaintings(@PathVariable Long userId);
}
