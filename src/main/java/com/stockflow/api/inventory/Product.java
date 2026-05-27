package com.stockflow.api.inventory;

import com.stockflow.api.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "sku"}))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false)
    private int priceCents;

    @Column(nullable = false)
    private int stockOnHand;

    @Column(nullable = false)
    private int stockReserved;

    @Column(nullable = false)
    private int lowStockThreshold;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Product() {
    }

    public Product(User owner, String sku, String name, int priceCents, int initialStock, int lowStockThreshold) {
        this.owner = owner;
        this.sku = sku;
        this.name = name;
        this.priceCents = priceCents;
        this.stockOnHand = initialStock;
        this.lowStockThreshold = lowStockThreshold;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getStockOnHand() {
        return stockOnHand;
    }

    public int getStockReserved() {
        return stockReserved;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public int availableStock() {
        return stockOnHand - stockReserved;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        stockOnHand += quantity;
        touch();
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (availableStock() < quantity) {
            throw new IllegalStateException("Not enough available stock");
        }
        stockReserved += quantity;
        touch();
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stockReserved < quantity) {
            throw new IllegalStateException("Cannot release more stock than reserved");
        }
        stockReserved -= quantity;
        touch();
    }

    public void sellReserved(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stockReserved < quantity) {
            throw new IllegalStateException("Cannot sell more stock than reserved");
        }
        stockReserved -= quantity;
        stockOnHand -= quantity;
        touch();
    }

    private void touch() {
        updatedAt = Instant.now();
    }
}
