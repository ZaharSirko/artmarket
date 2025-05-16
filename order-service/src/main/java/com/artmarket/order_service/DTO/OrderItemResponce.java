package com.artmarket.order_service.DTO;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderItemResponce(
         Long paintingId,
         BigDecimal price) implements Serializable {}
