package com.artmarket.order_service.event;


import com.artmarket.events.BaseEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderPaintingUpdatedEvent(
        Long orderId,
        String userId,
        BigDecimal itemsPrice,
        List<PaintingItem> paintings,
        ActionType action,
        LocalDateTime eventTime
) implements BaseEvent {

    public enum ActionType {
        ADDED, REMOVED
    }

    public record PaintingItem(
            Long paintingId,
            BigDecimal price
    ) {}
}
