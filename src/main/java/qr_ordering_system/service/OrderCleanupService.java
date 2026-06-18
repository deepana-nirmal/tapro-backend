package qr_ordering_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderCleanupService {

    private static final Logger log = LoggerFactory.getLogger(OrderCleanupService.class);

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldCompletedOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(2);
        long deleted = orderRepository.deleteByStatusInAndCreatedAtBefore(
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
                cutoff
        );

        log.info("Deleted {} completed/cancelled orders older than {}", deleted, cutoff);
    }
}
