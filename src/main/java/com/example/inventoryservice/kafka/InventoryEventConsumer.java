package com.example.inventoryservice.kafka;

import com.example.inventoryservice.service.InventoryService;
import com.example.ordertrackingcommon.model.InventoryEvent;
import com.example.ordertrackingcommon.model.InventoryStatus;
import com.example.ordertrackingcommon.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private static final String ORDER_CREATED_TOPIC = "orders_topic";
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";
    private static final String GROUP_ID = "inventory-service";

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public InventoryEventConsumer(InventoryService inventoryService,
                                  KafkaTemplate<String, InventoryEvent> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = ORDER_CREATED_TOPIC, groupId = GROUP_ID)
    public void consume(OrderEvent orderEvent) {
        if (orderEvent == null) {
            log.warn("Received null OrderEvent. Ignoring.");
            return;
        }
        log.info("Received OrderEvent: {}", orderEvent);

        Long orderId = orderEvent.getOrderId();
        String productId = orderEvent.getProductId();
        Integer qty = orderEvent.getQty();

        if (orderId == null || productId == null || qty == null) {
            log.error("OrderEvent missing required fields. orderId={}, productId={}, qty={}", orderId, productId, qty);
            InventoryEvent failed = new InventoryEvent();
            failed.setEventType("InventoryEvent");
            failed.setOrderId(orderId);
            failed.setProductId(productId);
            failed.setQty(qty);
            failed.setStatus(InventoryStatus.FAILED);
            kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, failed);
            return;
        }

        boolean reserved;
        try {
            reserved = inventoryService.reserveStock(productId, qty);
        } catch (Exception ex) {
            log.error("Error while reserving stock for productId={}, qty={}", productId, qty, ex);
            reserved = false;
        }

        InventoryEvent inventoryEvent = new InventoryEvent();
        inventoryEvent.setEventType("InventoryEvent");
        inventoryEvent.setOrderId(orderId);
        inventoryEvent.setProductId(productId);
        inventoryEvent.setQty(qty);
        inventoryEvent.setStatus(reserved ? InventoryStatus.RESERVED : InventoryStatus.FAILED);

        kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, inventoryEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish InventoryEvent for orderId={}, productId={}, qty={}",
                                orderId, productId, qty, ex);
                    } else {
                        log.info("Published InventoryEvent: {}", inventoryEvent);
                    }
                });
    }
}
