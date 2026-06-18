package qr_ordering_system.service;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderCleanupServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderCleanupService orderCleanupService;

    @Test
    void deleteOldCompletedOrders_shouldDeleteCompletedAndCancelledRecordsOlderThanTwoMonths() {
        orderCleanupService.deleteOldCompletedOrders();

        verify(orderRepository).deleteByStatusInAndCreatedAtBefore(
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        );
    }
}
