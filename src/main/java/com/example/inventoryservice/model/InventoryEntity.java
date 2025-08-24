package com.example.inventoryservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "inventory")
@Schema(description = "Inventory record for a product")
public class InventoryEntity {

    @Id
    @Column(name = "product_id", unique = true, nullable = false)
    @NotBlank
    @Schema(description = "Unique product identifier", example = "P001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @Column(name = "product_name")
    @Schema(description = "Human-readable product name", example = "Wireless Mouse")
    private String productName;

    @Column(name = "available_qty")
    @Min(0)
    @Schema(description = "Available quantity in stock", example = "120", minimum = "0")
    private Integer availableQty;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }
}
