package qr_ordering_system.service;

import org.springframework.stereotype.Component;

import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.model.OrderStatus;

@Component
public class OrderStatusTransitionValidator {

    public void validateKitchenOrOwnerTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.COMPLETED) {
            throw new BadRequestException("Order already completed");
        }

        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is cancelled");
        }

        if (current == OrderStatus.PENDING && next != OrderStatus.PREPARING) {
            throw new BadRequestException("Invalid status transition: PENDING -> " + next);
        }

        if (current == OrderStatus.PREPARING && next != OrderStatus.READY) {
            throw new BadRequestException("Invalid status transition: PREPARING -> " + next);
        }

        if (current == OrderStatus.READY && next != OrderStatus.COMPLETED) {
            throw new BadRequestException("Invalid status transition: READY -> " + next);
        }
    }
}
