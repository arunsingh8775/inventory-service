package com.example.inventoryservice.config;

import com.example.ordertrackingcommon.model.InventoryEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    private final ProducerFactory<String, InventoryEvent> producerFactory;

    public KafkaConfig(ProducerFactory<String, InventoryEvent> producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Bean
    public KafkaTemplate<String, InventoryEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory);
    }
}
