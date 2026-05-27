package com.stockflow.api.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @Min(1) int priceCents,
        @Min(0) int initialStock,
        @Min(0) int lowStockThreshold
) {
}
