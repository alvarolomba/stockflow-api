package com.stockflow.api.inventory.dto;

import com.stockflow.api.inventory.OrderItem;

public record OrderItemResponse(
        Long productId,
        String sku,
        String productName,
        int quantity,
        int unitPriceCents,
        int lineTotalCents
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPriceCents(),
                item.getLineTotalCents()
        );
    }
}
