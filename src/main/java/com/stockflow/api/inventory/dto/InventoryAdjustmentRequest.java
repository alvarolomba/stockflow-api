package com.stockflow.api.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryAdjustmentRequest(
        @NotNull Long productId,
        @Min(1) int quantity,
        String reason
) {
}
