package qr_ordering_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);

    List<Order> findByTenantId(Long tenantId);

    List<Order> findByTenantIdAndStatus(Long tenantId, OrderStatus status);

    List<Order> findByTenantIdAndTableNumber(Long tenantId, String tableNumber);

    List<Order> findByTenantIdAndStatusIn(Long tenantId, List<OrderStatus> status);

    @Query("""
            select o from Order o
            where o.tenantId = :tenantId
              and o.status in :statuses
              and (:from is null or o.createdAt >= :from)
              and (:to is null or o.createdAt < :to)
            order by o.createdAt desc
            """)
    List<Order> findOrdersForRestaurant(
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    long deleteByStatusInAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime cutoff);
}
