package com.stockflow.api.inventory;

import com.stockflow.api.users.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private int totalCents;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(User owner) {
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getTotalCents() {
        return totalCents;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(this, product, quantity, product.getPriceCents());
        items.add(item);
        totalCents += item.getLineTotalCents();
    }

    public void markPaid() {
        status = OrderStatus.PAID;
        touch();
    }

    public void markCanceled() {
        status = OrderStatus.CANCELED;
        touch();
    }

    private void touch() {
        updatedAt = Instant.now();
    }
}
