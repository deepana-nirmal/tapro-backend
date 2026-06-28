package qr_ordering_system.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    interface TenantOrderCountView {
        Long getTenantId();
        long getOrderCount();
    }

    interface TenantRevenueView {
        Long getTenantId();
        Double getTotalRevenue();
    }

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);

    List<Order> findByTenantId(Long tenantId);

    List<Order> findByTenantIdAndStatus(Long tenantId, OrderStatus status);

    List<Order> findByTenantIdAndTableNumber(Long tenantId, String tableNumber);

    List<Order> findByTenantIdAndStatusIn(Long tenantId, List<OrderStatus> status);

    List<Order> findByTenantIdAndStatusInOrderByCreatedAtDesc(Long tenantId, List<OrderStatus> statuses);

    List<Order> findByTenantIdAndStatusInAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long tenantId,
            List<OrderStatus> statuses,
            LocalDateTime from
    );

    List<Order> findByTenantIdAndStatusInAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long tenantId,
            List<OrderStatus> statuses,
            LocalDateTime to
    );

    List<Order> findByTenantIdAndStatusInAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long tenantId,
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    );

    default List<Order> findOrdersForRestaurant(
            Long tenantId,
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from == null && to == null) {
            return findByTenantIdAndStatusInOrderByCreatedAtDesc(tenantId, statuses);
        }

        if (from == null) {
            return findByTenantIdAndStatusInAndCreatedAtLessThanOrderByCreatedAtDesc(tenantId, statuses, to);
        }

        if (to == null) {
            return findByTenantIdAndStatusInAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(tenantId, statuses, from);
        }

        return findByTenantIdAndStatusInAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                tenantId,
                statuses,
                from,
                to
        );
    }

    @Query("""
            select o.tenantId as tenantId, count(o) as orderCount
            from Order o
            where o.tenantId in :tenantIds
              and o.status in :statuses
            group by o.tenantId
            """)
    List<TenantOrderCountView> countOrdersByTenantIdsAndStatuses(
            @Param("tenantIds") Collection<Long> tenantIds,
            @Param("statuses") List<OrderStatus> statuses
    );

    @Query("""
            select o.tenantId as tenantId, coalesce(sum(o.totalAmount), 0) as totalRevenue
            from Order o
            where o.tenantId in :tenantIds
              and o.status in :statuses
              and o.createdAt >= :from
              and o.createdAt < :to
            group by o.tenantId
            """)
    List<TenantRevenueView> sumRevenueByTenantIdsAndStatusesAndCreatedAtBetween(
            @Param("tenantIds") Collection<Long> tenantIds,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    default Map<Long, Long> countOrdersByTenantIdsAndStatusesAsMap(
            Collection<Long> tenantIds,
            List<OrderStatus> statuses
    ) {
        return countOrdersByTenantIdsAndStatuses(tenantIds, statuses)
                .stream()
                .collect(Collectors.toMap(TenantOrderCountView::getTenantId, TenantOrderCountView::getOrderCount));
    }

    default Map<Long, Double> sumRevenueByTenantIdsAndStatusesAndCreatedAtBetweenAsMap(
            Collection<Long> tenantIds,
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return sumRevenueByTenantIdsAndStatusesAndCreatedAtBetween(tenantIds, statuses, from, to)
                .stream()
                .collect(Collectors.toMap(
                        TenantRevenueView::getTenantId,
                        view -> view.getTotalRevenue() != null ? view.getTotalRevenue() : 0D
                ));
    }

    long deleteByStatusInAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime cutoff);
}
