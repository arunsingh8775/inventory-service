package com.example.inventoryservice.kafka;

import com.example.inventoryservice.model.InventoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, InventoryEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryEvent(InventoryEvent event) {
        kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish InventoryEvent {}", event, ex);
                    } else {
                        log.info("Published InventoryEvent {}", event);
                    }
                });
    }
}
