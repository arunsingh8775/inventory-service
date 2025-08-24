package com.example.inventoryservice.controller;

import com.example.inventoryservice.model.InventoryEntity;
import com.example.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Inventory Management", description = "Check stock and upsert inventory records")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @Operation(summary = "Get available stock for a product", description = "Returns the current available quantity for the given productId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock found",
                    content = @Content(schema = @Schema(type = "integer", format = "int32", example = "25"))),
            @ApiResponse(responseCode = "400", description = "Bad request (missing/blank productId)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found in inventory", content = @Content)
    })
    @GetMapping(value = "/{productId}", produces = "application/json")
    public ResponseEntity<Integer> getStock(
            @Parameter(description = "Unique product identifier", example = "P001")
            @PathVariable String productId) {
        if (productId == null || productId.isBlank()) return ResponseEntity.badRequest().build();
        Optional<InventoryEntity> inventoryOpt = inventoryService.getInventoryByProductId(productId.trim());
        return inventoryOpt.map(inv -> ResponseEntity.ok(inv.getAvailableQty()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create or update inventory", description = "Upserts an inventory record for a product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory saved",
                    content = @Content(schema = @Schema(implementation = InventoryEntity.class),
                            examples = @ExampleObject(
                                    name = "Saved inventory",
                                    value = """
                    {
                      "productId": "P001",
                      "productName": "Wireless Mouse",
                      "availableQty": 120
                    }
                    """
                            )))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<InventoryEntity> saveOrUpdate(
            @RequestBody(
                    required = true,
                    description = "Inventory payload to create or update",
                    content = @Content(schema = @Schema(implementation = InventoryEntity.class),
                            examples = @ExampleObject(
                                    name = "Upsert inventory",
                                    value = """
                    {
                      "productId": "P001",
                      "productName": "Wireless Mouse",
                      "availableQty": 120
                    }
                    """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody InventoryEntity inventoryEntity
    ) {
        if (inventoryEntity == null || inventoryEntity.getProductId() == null || inventoryEntity.getProductId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        InventoryEntity saved = inventoryService.saveOrUpdate(inventoryEntity);
        return ResponseEntity.ok(saved);
    }
}
