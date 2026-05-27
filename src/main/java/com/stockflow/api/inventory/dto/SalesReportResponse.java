package com.stockflow.api.inventory.dto;

public record SalesReportResponse(
        long paidOrders,
        long totalRevenueCents
) {
}
