package com.artmarket.config;

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
    String orders;
    String orderCreated;
    String orderStatusChanged;
    String orderUpdated;
    String orderPaintingUpdated;
    String shippingUpdated;
    String shippingStatusChanged;
    String shippingFailed;
}

