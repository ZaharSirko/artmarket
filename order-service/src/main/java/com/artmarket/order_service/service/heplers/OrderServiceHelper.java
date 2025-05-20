package com.artmarket.order_service.service.heplers;

import com.artmarket.order_service.DTO.*;
import com.artmarket.order_service.DTO.client.PaintingResponse;
import com.artmarket.order_service.DTO.client.UserResponse;
import com.artmarket.order_service.client.PaintingClient;
import com.artmarket.order_service.client.UserClient;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;
import com.artmarket.order_service.model.ShippingInfo;
import com.artmarket.order_service.model.enums.ShippingStatus;
import com.artmarket.order_service.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceHelper {
    private final OrderRepository orderRepository;
    private final PaintingClient paintingClient;
    private final UserClient userClient;

    public Order getOrderWithValidation(Long orderId, String bearerToken) {
        Order order = getOrderOrThrow(orderId);
        validateUserOwnsOrder(order, bearerToken);
        return order;
    }

    public Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", orderId);
                    return new EntityNotFoundException("Order not found");
                });
    }

    public void validateUserOwnsOrder(Order order, String bearerToken) {
        String currentUserId = getCurrentUser(bearerToken).keycloakId();
        if (!order.getUserId().equals(currentUserId)) {
            log.warn("User {} attempted to access order {} owned by {}",
                    currentUserId, order.getId(), order.getUserId());
            throw new AccessDeniedException("User does not own this order");
        }
    }

    public List<PaintingResponse> getPaintingsForOrder(OrderRequest request) {
        List<Long> paintingIds = request.items().stream()
                .map(OrderItemRequest::paintingId)
                .toList();
        return getPaintingsByIds(paintingIds);
    }

    public List<PaintingResponse> getPaintingsByIds(List<Long> paintingIds) {
        if (paintingIds.isEmpty()) {
            return Collections.emptyList();
        }
        return paintingClient.getPaintingsByIds(paintingIds);
    }

    public PaintingResponse getValidatedPainting(Long paintingId) {
        PaintingResponse painting = paintingClient.getPaintingById(paintingId);
        if (painting == null) {
            log.error("Painting not found with id: {}", paintingId);
            throw new EntityNotFoundException("Painting not found");
        }
        return painting;
    }

    public OrderItem findOrderItem(Order order, Long paintingId) {
        return order.getItems().stream()
                .filter(item -> item.getPaintingId().equals(paintingId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Painting {} not found in order {}", paintingId, order.getId());
                    return new EntityNotFoundException("Painting not found in order");
                });
    }

    public BigDecimal calculateTotalPrice(List<PaintingResponse> paintings) {
        return paintings.stream()
                .map(PaintingResponse::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal recalculateTotalPrice(Order order) {
        return order.getItems().stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<OrderItem> buildOrderItems(List<PaintingResponse> paintings, Order order) {
        return paintings.stream()
                .map(painting -> buildOrderItem(painting, order))
                .toList();
    }

    public OrderItem buildOrderItem(PaintingResponse painting, Order order) {
        return OrderItem.builder()
                .paintingId(painting.id())
                .price(painting.price())
                .order(order)
                .build();
    }

    public ShippingInfo buildShippingInfo(ShippingRequest request) {
        return ShippingInfo.builder()
                .recipientName(request.recipientName())
                .phone(request.phone())
                .city(request.city())
                .warehouse(request.warehouse())
                .shippingProvider(request.shippingProvider())
                .shippingStatus(ShippingStatus.NEW)
                .build();
    }

    public ShippingResponse buildShippingResponse(ShippingInfo shippingInfo) {
        return new ShippingResponse(
                shippingInfo.getShippingProvider(),
                shippingInfo.getTrackingNumber(),
                shippingInfo.getRecipientName(),
                shippingInfo.getPhone(),
                shippingInfo.getCity(),
                shippingInfo.getWarehouse(),
                shippingInfo.getShippingStatus()
        );
    }

    public List<PaintingResponse> getPaintingsForOrderItems(List<OrderItem> items) {
        List<Long> paintingIds = items.stream()
                .map(OrderItem::getPaintingId)
                .toList();
        return getPaintingsByIds(paintingIds);
    }

    public UserResponse getCurrentUser(String bearerToken) {
        try {
            return userClient.getCurrentUser("Bearer " + bearerToken);
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            try {
                throw new ServiceUnavailableException("User service unavailable");
            } catch (ServiceUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}