package qr_ordering_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.MenuItemStatus;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurant_Id(Long restaurantId);

    @Query("""
            select m
            from MenuItem m
            join m.category c
            where m.restaurant.id = :restaurantId
              and c.visible = true
              and m.status in :statuses
            """)
    List<MenuItem> findVisibleCustomerMenuByRestaurantIdAndStatusIn(
            @Param("restaurantId") Long restaurantId,
            @Param("statuses") List<MenuItemStatus> statuses
    );

    List<MenuItem> findByCategory_Id(Long categoryId);

    long countByCategory_Id(Long categoryId);
}
