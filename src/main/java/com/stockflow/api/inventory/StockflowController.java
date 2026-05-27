package com.stockflow.api.inventory;

import com.stockflow.api.inventory.dto.CreateOrderRequest;
import com.stockflow.api.inventory.dto.InventoryAdjustmentRequest;
import com.stockflow.api.inventory.dto.InventoryMovementResponse;
import com.stockflow.api.inventory.dto.OrderResponse;
import com.stockflow.api.inventory.dto.OrdersByStatusResponse;
import com.stockflow.api.inventory.dto.ProductRequest;
import com.stockflow.api.inventory.dto.ProductResponse;
import com.stockflow.api.inventory.dto.SalesReportResponse;
import com.stockflow.api.users.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockflowController {

    private final StockflowService stockflowService;

    public StockflowController(StockflowService stockflowService) {
        this.stockflowService = stockflowService;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    ProductResponse createProduct(@AuthenticationPrincipal User owner, @Valid @RequestBody ProductRequest request) {
        return stockflowService.createProduct(owner, request);
    }

    @GetMapping("/products")
    List<ProductResponse> listProducts(@AuthenticationPrincipal User owner) {
        return stockflowService.listProducts(owner);
    }

    @PostMapping("/inventory/movements")
    @ResponseStatus(HttpStatus.CREATED)
    InventoryMovementResponse addInventory(@AuthenticationPrincipal User owner, @Valid @RequestBody InventoryAdjustmentRequest request) {
        return stockflowService.addInventory(owner, request);
    }

    @GetMapping("/inventory/movements")
    List<InventoryMovementResponse> listInventoryMovements(@AuthenticationPrincipal User owner) {
        return stockflowService.listInventoryMovements(owner);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse createOrder(@AuthenticationPrincipal User owner, @Valid @RequestBody CreateOrderRequest request) {
        return stockflowService.createOrder(owner, request);
    }

    @GetMapping("/orders")
    List<OrderResponse> listOrders(@AuthenticationPrincipal User owner) {
        return stockflowService.listOrders(owner);
    }

    @PatchMapping("/orders/{orderId}/pay")
    OrderResponse payOrder(@AuthenticationPrincipal User owner, @PathVariable Long orderId) {
        return stockflowService.payOrder(owner, orderId);
    }

    @PatchMapping("/orders/{orderId}/cancel")
    OrderResponse cancelOrder(@AuthenticationPrincipal User owner, @PathVariable Long orderId) {
        return stockflowService.cancelOrder(owner, orderId);
    }

    @GetMapping("/reports/low-stock")
    List<ProductResponse> lowStockProducts(@AuthenticationPrincipal User owner) {
        return stockflowService.lowStockProducts(owner);
    }

    @GetMapping("/reports/sales")
    SalesReportResponse salesReport(@AuthenticationPrincipal User owner) {
        return stockflowService.salesReport(owner);
    }

    @GetMapping("/reports/orders-by-status")
    OrdersByStatusResponse ordersByStatus(@AuthenticationPrincipal User owner) {
        return stockflowService.ordersByStatus(owner);
    }
}
