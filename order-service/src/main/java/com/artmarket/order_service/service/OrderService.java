package com.artmarket.order_service.service;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final PaintingClient paintingClient;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        List<Long> paintingIds = request.items().stream()
                .map(OrderItemRequest::paintingId)
                .toList();

        List<PaintingResponse> paintingResponse = paintingClient.getPaintingsByIds(paintingIds);

        BigDecimal totalPrice = paintingResponse.stream()
                .map(PaintingResponse::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ShippingInfo shippingInfo = ShippingInfo.builder()
                .recipientName(request.shipping().recipientName())
                .phone(request.shipping().phone())
                .city(request.shipping().city())
                .warehouse(request.shipping().warehouse())
                .shippingProvider(request.shipping().shippingProvider())
                .shippingStatus(ShippingStatus.NEW)
                .build();

        Order order = Order.builder()
                .userId(request.userId())
                .status(OrderStatus.NEW)
                .totalPrice(totalPrice)
                .shippingInfo(shippingInfo)
                .build();

        List<OrderItem> items = paintingResponse.stream()
                .map(itemDto -> OrderItem.builder()
                        .paintingId(itemDto.id())
                        .price(itemDto.price())
                        .order(order)
                        .build()
                ).collect(Collectors.toList());

        order.setItems(items);

        Order savedOrder = orderRepository.save(order);


        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse addPaintingToOrder(Long orderId, OrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Cannot update order with status: " + order.getStatus());
        }

        PaintingResponse painting = paintingClient.getPaintingsByIds(List.of(request.paintingId()))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Painting not found"));


        OrderItem item = OrderItem.builder()
                .paintingId(painting.id())
                .price(painting.price())
                .order(order)
                .build();

        order.getItems().add(item);

        order.setTotalPrice(order.getTotalPrice().add(painting.price()));

        orderRepository.save(order);

        return mapToResponse(order);
    }



    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersForCurrentUser(String bearerToken) {
        UserResponse user = userClient.getCurrentUser(bearerToken);
        List<Order> orders = orderRepository.findAllByUserId(user.keycloakId());

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }


    public OrderResponse mapToResponse(Order order) {
        List<Long> paintingIds = order.getItems().stream()
                .map(OrderItem::getPaintingId)
                .toList();

        List<PaintingResponse> paintingResponse = paintingClient.getPaintingsByIds(paintingIds);

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
                paintingResponse,
                shipping
        );
    }
}
