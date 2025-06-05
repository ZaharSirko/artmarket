package com.artmarket.order_service.model;


import com.artmarket.dto.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_gen")
    @SequenceGenerator(name = "order_gen", sequenceName = "order_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    Long id;
    String userId;

    @Enumerated(EnumType.STRING)
    OrderStatus status;

    BigDecimal itemsPrice;
    BigDecimal deliveryPrice;
    BigDecimal totalPrice;

    @Embedded
    ShippingInfo shippingInfo;

    @Builder.Default
    Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderStatusHistory> statusHistory;
}