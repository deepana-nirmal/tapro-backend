package qr_ordering_system.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        orderRepository.save(createOrder(1L, OrderStatus.COMPLETED, LocalDateTime.of(2026, 6, 27, 9, 0), 15.0));
        orderRepository.save(createOrder(1L, OrderStatus.COMPLETED, LocalDateTime.of(2026, 6, 27, 11, 0), 20.0));
        orderRepository.save(createOrder(1L, OrderStatus.CANCELLED, LocalDateTime.of(2026, 6, 26, 15, 0), 5.0));
        orderRepository.save(createOrder(2L, OrderStatus.COMPLETED, LocalDateTime.of(2026, 6, 27, 10, 0), 30.0));
    }

    @Test
    void findOrdersForRestaurant_withoutDateFilters_returnsAllMatchingStatuses() {
        List<Order> result = orderRepository.findOrdersForRestaurant(
                1L,
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
                null,
                null
        );

        assertEquals(3, result.size());
        assertEquals(LocalDateTime.of(2026, 6, 27, 11, 0), result.get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 6, 27, 9, 0), result.get(1).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 6, 26, 15, 0), result.get(2).getCreatedAt());
    }

    @Test
    void findOrdersForRestaurant_withFromOnly_appliesLowerBound() {
        List<Order> result = orderRepository.findOrdersForRestaurant(
                1L,
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
                LocalDateTime.of(2026, 6, 27, 0, 0),
                null
        );

        assertEquals(2, result.size());
        assertEquals(LocalDateTime.of(2026, 6, 27, 11, 0), result.get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 6, 27, 9, 0), result.get(1).getCreatedAt());
    }

    @Test
    void findOrdersForRestaurant_withToOnly_appliesUpperBound() {
        List<Order> result = orderRepository.findOrdersForRestaurant(
                1L,
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
                null,
                LocalDateTime.of(2026, 6, 27, 10, 0)
        );

        assertEquals(2, result.size());
        assertEquals(LocalDateTime.of(2026, 6, 27, 9, 0), result.get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 6, 26, 15, 0), result.get(1).getCreatedAt());
    }

    @Test
    void findOrdersForRestaurant_withFromAndTo_appliesBothBounds() {
        List<Order> result = orderRepository.findOrdersForRestaurant(
                1L,
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
                LocalDateTime.of(2026, 6, 27, 0, 0),
                LocalDateTime.of(2026, 6, 27, 10, 0)
        );

        assertEquals(1, result.size());
        assertEquals(LocalDateTime.of(2026, 6, 27, 9, 0), result.get(0).getCreatedAt());
    }

    private Order createOrder(Long tenantId, OrderStatus status, LocalDateTime createdAt, double totalAmount) {
        Order order = new Order();
        order.setTenantId(tenantId);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        order.setTableNumber("A1");
        order.setTotalAmount(totalAmount);
        return order;
    }
}
