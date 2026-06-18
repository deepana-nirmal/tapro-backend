package qr_ordering_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import qr_ordering_system.model.RestaurantTable;

public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurantId(Long restaurantId);

    Optional<RestaurantTable> findByIdAndRestaurantId(Long id, Long restaurantId);

    boolean existsByRestaurantIdAndTableNumberIgnoreCase(Long restaurantId, String tableNumber);

    boolean existsByRestaurantIdAndTableNumberIgnoreCaseAndIdNot(Long restaurantId, String tableNumber, Long id);
}
