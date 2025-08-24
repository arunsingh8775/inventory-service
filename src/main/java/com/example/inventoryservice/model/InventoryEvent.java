package com.example.inventoryservice.model;

public class InventoryEvent {
    private String eventType;     // "InventoryEvent"
    private Long orderId;
    private String productId;
    private Integer qty;
    private InventoryStatus status;

    public InventoryEvent() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "InventoryEvent{" +
                "eventType='" + eventType + '\'' +
                ", orderId=" + orderId +
                ", productId='" + productId + '\'' +
                ", qty=" + qty +
                ", status=" + status +
                '}';
    }
}
