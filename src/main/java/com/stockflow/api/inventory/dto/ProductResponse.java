package com.stockflow.api.inventory.dto;

import com.stockflow.api.inventory.Product;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        int priceCents,
        int stockOnHand,
        int stockReserved,
        int availableStock,
        int lowStockThreshold
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPriceCents(),
                product.getStockOnHand(),
                product.getStockReserved(),
                product.availableStock(),
                product.getLowStockThreshold()
        );
    }
}
