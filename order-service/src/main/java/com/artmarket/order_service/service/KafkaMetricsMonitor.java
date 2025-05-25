package com.artmarket.order_service.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMetricsMonitor {

    private final MeterRegistry meterRegistry;

    @KafkaListener(id = "metricsListener", topics = "#{'${kafka.topics.order-created},${kafka.topics.order-status-changed},${kafka.topics.order-updated},${kafka.topics.shipping-updated}'.split(',')}")
    public void monitorEvents(ConsumerRecord<?, ?> record) {
        String topic = record.topic();
        meterRegistry.counter("kafka.consumer.messages", "topic", topic).increment();
        log.debug("Received message from topic {} at offset {}", topic, record.offset());
    }

}