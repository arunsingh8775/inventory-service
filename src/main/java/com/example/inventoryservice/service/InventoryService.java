package com.example.inventoryservice.service;

import com.example.inventoryservice.model.InventoryEntity;
import com.example.inventoryservice.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Optional<InventoryEntity> getInventoryByProductId(String productId) {
        if (productId == null || productId.isBlank()) return Optional.empty();
        return inventoryRepository.findByProductId(productId.trim());
    }

    @Transactional
    public InventoryEntity saveOrUpdate(InventoryEntity incoming) {
        if (incoming == null) throw new IllegalArgumentException("Inventory payload is required");
        if (incoming.getProductId() == null || incoming.getProductId().isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }

        final String productId = incoming.getProductId().trim();

        InventoryEntity entity = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    InventoryEntity fresh = new InventoryEntity();
                    fresh.setProductId(productId);
                    if (incoming.getAvailableQty() == null) {
                        fresh.setAvailableQty(0);
                    }
                    return fresh;
                });

        if (incoming.getProductName() != null) {
            entity.setProductName(incoming.getProductName());
        }
        if (incoming.getAvailableQty() != null) {
            entity.setAvailableQty(incoming.getAvailableQty());
        }

        InventoryEntity saved = inventoryRepository.save(entity);
        log.debug("Saved inventory. productId={}, availableQty={}", saved.getProductId(), saved.getAvailableQty());
        return saved;
    }

    @Transactional
    public boolean reserveStock(String productId, int qty) {
        if (productId == null || productId.isBlank() || qty <= 0) return false;
        int updated = inventoryRepository.decrementStockIfAvailable(productId.trim(), qty);
        if (updated == 1) {
            log.info("Reserved {} units for productId={}", qty, productId);
            return true;
        } else {
            log.info("Insufficient stock for productId={} (requested {}). Reservation failed.", productId, qty);
            return false;
        }
    }
}
