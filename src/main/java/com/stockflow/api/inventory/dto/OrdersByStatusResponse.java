package com.stockflow.api.inventory.dto;

public record OrdersByStatusResponse(
        long pending,
        long paid,
        long canceled
) {
}
