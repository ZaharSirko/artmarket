package com.artmarket.order_service.service.heplers;

import com.artmarket.order_service.DTO.OrderItemRequest;
import com.artmarket.order_service.DTO.OrderRequest;
import com.artmarket.order_service.DTO.OrderResponse;
import com.artmarket.order_service.DTO.ShippingResponse;
import com.artmarket.order_service.DTO.client.PaintingResponse;
import com.artmarket.order_service.DTO.client.UserResponse;
import com.artmarket.order_service.client.PaintingClient;
import com.artmarket.order_service.client.UserClient;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;
import com.artmarket.order_service.model.ShippingInfo;
import com.artmarket.order_service.model.enums.OrderStatus;
import com.artmarket.order_service.model.enums.ShippingStatus;
import com.artmarket.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderServiceHelper {

    private final OrderRepository orderRepository;
    private final PaintingClient paintingClient;
    private final UserClient userClient;

    public Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public void validateOrderIsNew(Order order) {
        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Cannot update order with status: " + order.getStatus());
        }
    }

    public PaintingResponse getPaintingById(Long paintingId) {
        return paintingClient.getPaintingById(paintingId);
    }

    public List<PaintingResponse> getPaintingsByIds(List<Long> paintingIds) {
        return paintingClient.getPaintingsByIds(paintingIds);
    }

    public OrderItem buildOrderItem(PaintingResponse painting, Order order) {
        return OrderItem.builder()
                .paintingId(painting.id())
                .price(painting.price())
                .order(order)
                .build();
    }

    public BigDecimal calculateTotalPrice(List<PaintingResponse> paintings) {
        return paintings.stream()
                .map(PaintingResponse::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UserResponse getCurrentUser(String bearerToken) {
        return userClient.getCurrentUser("Bearer " + bearerToken);
    }
}
