package com.artmarket.order_service.event;

import java.time.LocalDateTime;

public interface BaseEvent {
    Long orderId();
    String userId();
    LocalDateTime eventTime();
}
