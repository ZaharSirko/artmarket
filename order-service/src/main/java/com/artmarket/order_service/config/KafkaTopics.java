package com.artmarket.order_service.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KafkaTopics {
     String orderCreated;
     String orderStatusChanged;
     String orderUpdated;
     String shippingUpdated;
}
