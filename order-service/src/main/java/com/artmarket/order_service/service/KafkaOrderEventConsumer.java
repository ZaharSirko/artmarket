//package com.artmarket.order_service.service;
//
//import com.artmarket.order_service.event.OrderStatusChangedEvent;
//import com.artmarket.order_service.event.ShippingUpdatedEvent;
//import com.artmarket.order_service.model.Order;
//import com.artmarket.order_service.model.ShippingInfo;
//import com.artmarket.order_service.model.enums.OrderStatus;
//import com.artmarket.order_service.model.enums.ShippingStatus;
//import com.artmarket.order_service.repository.OrderRepository;
//import com.artmarket.order_service.service.heplers.OrderServiceHelper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class KafkaOrderEventConsumer {
//    private final OrderRepository orderRepository;
//    private final OrderServiceHelper helper;
//
//    @KafkaListener(topics = "${kafka.topics.order-status-changed}", groupId = "order-service-group")
//    public void consumeOrderStatusChangedEvent(OrderStatusChangedEvent event) {
//        log.info("Received OrderStatusChangedEvent for order {}: new status {}", event.orderId(), event.newStatus());
//
//        Order order = orderRepository.findById(event.orderId())
//                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + event.orderId()));
//
//        OrderStatus newStatus = OrderStatus.valueOf(event.newStatus());
//        order.setStatus(newStatus);
//        orderRepository.save(order);
//
//        log.info("Updated status for order {} to {}", order.getId(), newStatus);
//    }
//
//    @KafkaListener(topics = "${kafka.topics.shipping-updated}", groupId = "order-service-group")
//    public void consumeShippingUpdatedEvent(ShippingUpdatedEvent event) {
//        log.info("Received ShippingUpdatedEvent for order {}: provider {}, tracking {}",
//                event.orderId(), event.provider(), event.trackingNumber());
//
//        Order order = orderRepository.findById(event.orderId())
//                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + event.orderId()));
//
//        ShippingInfo shippingInfo = order.getShippingInfo();
//        if (shippingInfo == null) {
//            shippingInfo = new ShippingInfo();
//        }
//
//        shippingInfo.setShippingProvider(event.provider());
//        shippingInfo.setTrackingNumber(event.trackingNumber());
//        shippingInfo.setShippingStatus(ShippingStatus.valueOf(event.status()));
//
//        order.setShippingInfo(shippingInfo);
//        orderRepository.save(order);
//
//        log.info("Updated shipping info for order {}", order.getId());
//    }
//}
