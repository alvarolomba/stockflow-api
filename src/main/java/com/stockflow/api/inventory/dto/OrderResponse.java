package com.stockflow.api.inventory.dto;

import com.stockflow.api.inventory.CustomerOrder;
import com.stockflow.api.inventory.OrderStatus;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        int totalCents,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalCents(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
