package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, String> {

    Optional<InventoryEntity> findByProductId(String productId);
    boolean existsByProductId(String productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update InventoryEntity i
              set i.availableQty = i.availableQty - :qty
            where i.productId = :productId
              and i.availableQty >= :qty
           """)
    int decrementStockIfAvailable(@Param("productId") String productId, @Param("qty") int qty);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryEntity i where i.productId = :productId")
    Optional<InventoryEntity> findByProductIdForUpdate(@Param("productId") String productId);
}
