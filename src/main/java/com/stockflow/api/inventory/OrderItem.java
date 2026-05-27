package com.stockflow.api.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int unitPriceCents;

    @Column(nullable = false)
    private int lineTotalCents;

    protected OrderItem() {
    }

    public OrderItem(CustomerOrder order, Product product, int quantity, int unitPriceCents) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;
        this.lineTotalCents = quantity * unitPriceCents;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPriceCents() {
        return unitPriceCents;
    }

    public int getLineTotalCents() {
        return lineTotalCents;
    }
}
