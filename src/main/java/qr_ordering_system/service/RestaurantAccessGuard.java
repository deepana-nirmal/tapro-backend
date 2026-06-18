package qr_ordering_system.service;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.RestaurantRepository;

@Service
@RequiredArgsConstructor
public class RestaurantAccessGuard {

    public static final String SUSPENDED_MESSAGE =
            "Payment overdue. Please contact your restaurant owner or Tapro support.";

    private static final Set<Role> RESTAURANT_SCOPED_ROLES = EnumSet.of(
            Role.ADMIN,
            Role.OWNER,
            Role.STAFF,
            Role.KITCHEN,
            Role.CASHIER
    );

    private final RestaurantRepository restaurantRepository;

    public boolean isRestaurantScopedRole(Role role) {
        return role != null && RESTAURANT_SCOPED_ROLES.contains(role);
    }

    public void ensureLoginAllowed(User user) {
        if (!isRestaurantScopedRole(user.getRole())) {
            return;
        }

        Restaurant restaurant = requireAssignedRestaurant(user);
        if (restaurant.getStatus() == RestaurantStatus.SUSPENDED) {
            throw new BadRequestException(SUSPENDED_MESSAGE);
        }
    }

    public void ensureRestaurantAccessAllowed(User user) {
        if (!isRestaurantScopedRole(user.getRole())) {
            return;
        }

        Restaurant restaurant = requireAssignedRestaurant(user);
        if (restaurant.getStatus() == RestaurantStatus.SUSPENDED) {
            throw new AccessDeniedException(SUSPENDED_MESSAGE);
        }
    }

    public Restaurant requireActiveRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (restaurant.getStatus() == RestaurantStatus.SUSPENDED) {
            throw new BadRequestException("Restaurant is unavailable");
        }

        return restaurant;
    }

    private Restaurant requireAssignedRestaurant(User user) {
        if (user.getRestaurant() == null) {
            throw new BadRequestException("User is not assigned to a restaurant");
        }

        return user.getRestaurant();
    }
}
