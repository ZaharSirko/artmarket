package com.artmarket.order_service.service;

import com.artmarket.order_service.DTO.OrderItemRequest;
import com.artmarket.order_service.DTO.OrderRequest;
import com.artmarket.order_service.DTO.OrderResponse;
import com.artmarket.order_service.DTO.ShippingResponse;
import com.artmarket.order_service.DTO.client.PaintingResponse;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;
import com.artmarket.order_service.model.ShippingInfo;
import com.artmarket.order_service.model.enums.OrderStatus;
import com.artmarket.order_service.model.enums.ShippingStatus;
import com.artmarket.order_service.repository.OrderRepository;
import com.artmarket.order_service.service.heplers.OrderServiceHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderServiceHelper helper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request,String bearerToken) {
        List<Long> paintingIds = request.items().stream()
                .map(OrderItemRequest::paintingId)
                .toList();

        List<PaintingResponse> paintings = helper.getPaintingsByIds(paintingIds);
        BigDecimal totalPrice = helper.calculateTotalPrice(paintings);

        ShippingInfo shippingInfo = ShippingInfo.builder()
                .recipientName(request.shipping().recipientName())
                .phone(request.shipping().phone())
                .city(request.shipping().city())
                .warehouse(request.shipping().warehouse())
                .shippingProvider(request.shipping().shippingProvider())
                .shippingStatus(ShippingStatus.NEW)
                .build();

        Order order = Order.builder()
                .userId(helper.getCurrentUser(bearerToken).keycloakId())
                .status(OrderStatus.NEW)
                .totalPrice(totalPrice)
                .shippingInfo(shippingInfo)
                .build();

        List<OrderItem> items = paintings.stream()
                .map(painting -> helper.buildOrderItem(painting, order))
                .toList();

        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse addPaintingToOrder(Long orderId, OrderItemRequest request) {
        Order order = helper.getOrderOrThrow(orderId);
        helper.validateOrderIsNew(order);

        PaintingResponse painting = helper.getPaintingById(request.paintingId());
        OrderItem item = helper.buildOrderItem(painting, order);

        order.getItems().add(item);
        order.setTotalPrice(order.getTotalPrice().add(painting.price()));
        orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse removePaintingFromOrder(Long orderId, OrderItemRequest request) {
        Order order = helper.getOrderOrThrow(orderId);
        helper.validateOrderIsNew(order);

        OrderItem itemToRemove = order.getItems().stream()
                .filter(item -> item.getPaintingId().equals(request.paintingId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Painting not found in order"));

        order.getItems().remove(itemToRemove);
        order.setTotalPrice(order.getTotalPrice().subtract(itemToRemove.getPrice()));

        orderRepository.save(order);

        return mapToResponse(order);
    }


    public OrderResponse getOrderById(Long id) {
        Order order = helper.getOrderOrThrow(id);
        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersForCurrentUser(String bearerToken) {
        String userId = helper.getCurrentUser(bearerToken).keycloakId();
        return orderRepository.findAllByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse mapToResponse(Order order) {
        List<Long> paintingIds = order.getItems().stream()
                .map(OrderItem::getPaintingId)
                .toList();

        List<PaintingResponse> paintings = helper.getPaintingsByIds(paintingIds);

        ShippingResponse shipping = new ShippingResponse(
                order.getShippingInfo().getShippingProvider(),
                order.getShippingInfo().getTrackingNumber(),
                order.getShippingInfo().getRecipientName(),
                order.getShippingInfo().getPhone(),
                order.getShippingInfo().getCity(),
                order.getShippingInfo().getWarehouse(),
                order.getShippingInfo().getShippingStatus()
        );

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                paintings,
                shipping
        );
    }
}
