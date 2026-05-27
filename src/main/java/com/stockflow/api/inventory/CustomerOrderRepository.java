package com.stockflow.api.inventory;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByOwnerIdOrderByIdDesc(Long ownerId);

    Optional<CustomerOrder> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, OrderStatus status);

    @Query("""
            select coalesce(sum(o.totalCents), 0)
            from CustomerOrder o
            where o.owner.id = :ownerId
              and o.status = :status
            """)
    long sumRevenueCentsByStatus(Long ownerId, OrderStatus status);
}
