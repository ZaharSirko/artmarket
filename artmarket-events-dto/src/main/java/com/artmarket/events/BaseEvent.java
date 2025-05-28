package com.artmarket.events;

import java.time.LocalDateTime;

public interface BaseEvent {
    Long orderId();
    String userId();
    LocalDateTime eventTime();
}
