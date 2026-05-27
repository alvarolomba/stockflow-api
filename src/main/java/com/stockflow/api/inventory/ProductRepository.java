package com.stockflow.api.inventory;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByOwnerIdOrderByIdDesc(Long ownerId);

    Optional<Product> findByIdAndOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.owner.id = :ownerId")
    Optional<Product> findByIdAndOwnerIdForUpdate(Long id, Long ownerId);

    boolean existsByOwnerIdAndSku(Long ownerId, String sku);

    @Query("""
            select p from Product p
            where p.owner.id = :ownerId
              and (p.stockOnHand - p.stockReserved) <= p.lowStockThreshold
            order by p.name asc
            """)
    List<Product> findLowStockProducts(Long ownerId);
}
