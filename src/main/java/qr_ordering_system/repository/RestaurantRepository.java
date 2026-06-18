package qr_ordering_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import qr_ordering_system.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    java.util.List<Restaurant> findAllByOrderByNameAsc();
}
