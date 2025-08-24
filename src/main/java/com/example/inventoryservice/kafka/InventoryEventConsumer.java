package com.example.paymentservice.kafka;

import com.example.ordertrackingcommon.model.InventoryEvent;
import com.example.ordertrackingcommon.model.InventoryStatus;
import com.example.ordertrackingcommon.model.PaymentEvent;
import com.example.orderservice.model.OrderEntity;
import com.example.orderservice.model.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryResultConsumer {

    private final EntityManager entityManager;   // Direct JPA access to order DB
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.topics.events:payment-events}")
    private String paymentEventsTopic;

    @Transactional
    @KafkaListener(topics = "inventory-events", groupId = "payment-service")
    public void onInventoryEvent(InventoryEvent inventoryEvent) {
        if (inventoryEvent == null) {
            log.warn("Received null InventoryEvent. Ignoring.");
            return;
        }
        log.info("Received InventoryEvent: {}", inventoryEvent);

        // Fetch the existing order from orders DB
        OrderEntity order = entityManager.find(OrderEntity.class, inventoryEvent.getOrderId());
        if (order == null) {
            log.error("Order not found for id={}", inventoryEvent.getOrderId());
            return;
        }

        // Decide payment outcome
        boolean ok = inventoryEvent.getStatus() == InventoryStatus.RESERVED;

        order.setPaymentStatus(ok ? PaymentStatus.PAID : PaymentStatus.NOT_PAID);
        order.setPaymentDate(LocalDateTime.now());
        order.setTotalAmount(inventoryEvent.getAmount()); // if InventoryEvent carries amount

        entityManager.merge(order);   // update order
        log.info("Updated orderId={} with paymentStatus={} and paymentDate={}",
                order.getOrderId(), order.getPaymentStatus(), order.getPaymentDate());

        // Build and publish PaymentEvent
        PaymentEvent evt = new PaymentEvent();
        evt.setEventType(ok ? "PaymentProcessed" : "PaymentFailed");
        evt.setOrderId(order.getOrderId());
        evt.setAmount(order.getTotalAmount());
        evt.setReason(ok ? null : "Inventory reservation failed");

        kafkaTemplate.send(paymentEventsTopic, String.valueOf(order.getOrderId()), evt)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish PaymentEvent for orderId={}", order.getOrderId(), ex);
                    } else {
                        log.info("Published {} for orderId={} to {}",
                                evt.getEventType(), order.getOrderId(), paymentEventsTopic);
                    }
                });
    }
}
