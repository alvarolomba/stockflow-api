package com.stockflow.api.inventory;

import com.stockflow.api.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryMovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InventoryMovement() {
    }

    public InventoryMovement(User owner, Product product, InventoryMovementType type, int quantity, String reason) {
        this.owner = owner;
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public InventoryMovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
