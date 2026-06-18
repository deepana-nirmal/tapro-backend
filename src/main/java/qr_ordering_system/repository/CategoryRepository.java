package qr_ordering_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import qr_ordering_system.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByRestaurantId(Long restaurantId);

    List<Category> findByRestaurantIdAndVisibleTrue(Long restaurantId);
}
