package qr_ordering_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import qr_ordering_system.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}