package com.stockflow.api.inventory;

import com.stockflow.api.inventory.dto.CreateOrderRequest;
import com.stockflow.api.inventory.dto.InventoryAdjustmentRequest;
import com.stockflow.api.inventory.dto.InventoryMovementResponse;
import com.stockflow.api.inventory.dto.OrderItemRequest;
import com.stockflow.api.inventory.dto.OrderResponse;
import com.stockflow.api.inventory.dto.OrdersByStatusResponse;
import com.stockflow.api.inventory.dto.ProductRequest;
import com.stockflow.api.inventory.dto.ProductResponse;
import com.stockflow.api.inventory.dto.SalesReportResponse;
import com.stockflow.api.users.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockflowService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;
    private final CustomerOrderRepository orderRepository;

    public StockflowService(
            ProductRepository productRepository,
            InventoryMovementRepository movementRepository,
            CustomerOrderRepository orderRepository
    ) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ProductResponse createProduct(User owner, ProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (productRepository.existsByOwnerIdAndSku(owner.getId(), sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }

        Product product = productRepository.save(new Product(
                owner,
                sku,
                request.name().trim(),
                request.priceCents(),
                request.initialStock(),
                request.lowStockThreshold()
        ));

        if (request.initialStock() > 0) {
            movementRepository.save(new InventoryMovement(
                    owner,
                    product,
                    InventoryMovementType.ADDED,
                    request.initialStock(),
                    "Initial stock"
            ));
        }

        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(User owner) {
        return productRepository.findByOwnerIdOrderByIdDesc(owner.getId())
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> listInventoryMovements(User owner) {
        return movementRepository.findByOwnerIdOrderByIdDesc(owner.getId())
                .stream()
                .map(InventoryMovementResponse::from)
                .toList();
    }

    @Transactional
    public InventoryMovementResponse addInventory(User owner, InventoryAdjustmentRequest request) {
        Product product = findProductForUpdate(owner, request.productId());
        product.addStock(request.quantity());

        InventoryMovement movement = movementRepository.save(new InventoryMovement(
                owner,
                product,
                InventoryMovementType.ADDED,
                request.quantity(),
                request.reason()
        ));
        return InventoryMovementResponse.from(movement);
    }

    @Transactional
    public OrderResponse createOrder(User owner, CreateOrderRequest request) {
        Map<Long, Integer> quantitiesByProduct = new HashMap<>();
        for (OrderItemRequest item : request.items()) {
            quantitiesByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }

        CustomerOrder order = new CustomerOrder(owner);
        quantitiesByProduct.forEach((productId, quantity) -> {
            Product product = findProductForUpdate(owner, productId);
            if (product.availableStock() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough available stock for SKU " + product.getSku());
            }

            product.reserve(quantity);
            order.addItem(product, quantity);
            movementRepository.save(new InventoryMovement(
                    owner,
                    product,
                    InventoryMovementType.RESERVED,
                    quantity,
                    "Reserved for order"
            ));
        });

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(User owner) {
        return orderRepository.findByOwnerIdOrderByIdDesc(owner.getId())
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse payOrder(User owner, Long orderId) {
        CustomerOrder order = findOrder(owner, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending orders can be paid");
        }

        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.sellReserved(item.getQuantity());
            movementRepository.save(new InventoryMovement(
                    owner,
                    product,
                    InventoryMovementType.SOLD,
                    item.getQuantity(),
                    "Order paid"
            ));
        });
        order.markPaid();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(User owner, Long orderId) {
        CustomerOrder order = findOrder(owner, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending orders can be canceled");
        }

        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.release(item.getQuantity());
            movementRepository.save(new InventoryMovement(
                    owner,
                    product,
                    InventoryMovementType.RELEASED,
                    item.getQuantity(),
                    "Order canceled"
            ));
        });
        order.markCanceled();
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStockProducts(User owner) {
        return productRepository.findLowStockProducts(owner.getId())
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesReportResponse salesReport(User owner) {
        return new SalesReportResponse(
                orderRepository.countByOwnerIdAndStatus(owner.getId(), OrderStatus.PAID),
                orderRepository.sumRevenueCentsByStatus(owner.getId(), OrderStatus.PAID)
        );
    }

    @Transactional(readOnly = true)
    public OrdersByStatusResponse ordersByStatus(User owner) {
        return new OrdersByStatusResponse(
                orderRepository.countByOwnerIdAndStatus(owner.getId(), OrderStatus.PENDING),
                orderRepository.countByOwnerIdAndStatus(owner.getId(), OrderStatus.PAID),
                orderRepository.countByOwnerIdAndStatus(owner.getId(), OrderStatus.CANCELED)
        );
    }

    private Product findProduct(User owner, Long productId) {
        return productRepository.findByIdAndOwnerId(productId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Product findProductForUpdate(User owner, Long productId) {
        return productRepository.findByIdAndOwnerIdForUpdate(productId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private CustomerOrder findOrder(User owner, Long orderId) {
        return orderRepository.findByIdAndOwnerId(orderId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
