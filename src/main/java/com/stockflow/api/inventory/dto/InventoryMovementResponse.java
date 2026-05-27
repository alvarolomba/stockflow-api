package com.stockflow.api.inventory.dto;

import com.stockflow.api.inventory.InventoryMovement;
import com.stockflow.api.inventory.InventoryMovementType;
import java.time.Instant;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        String sku,
        InventoryMovementType type,
        int quantity,
        String reason,
        Instant createdAt
) {
    public static InventoryMovementResponse from(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }
}
