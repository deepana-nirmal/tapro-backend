package qr_ordering_system.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import qr_ordering_system.model.Category;
import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.model.Restaurant;

class MenuItemMapperTest {

    private final MenuItemMapper mapper = new MenuItemMapper();

    @Test
    void toDto_includesCategoryName() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Tapro Bistro");

        Category category = new Category();
        category.setId(2L);
        category.setName("Rice");
        category.setRestaurant(restaurant);

        MenuItem item = new MenuItem();
        item.setId(3L);
        item.setName("Fried Rice");
        item.setStatus(MenuItemStatus.AVAILABLE);
        item.setCategory(category);
        item.setRestaurant(restaurant);

        var dto = mapper.toDto(item);

        assertEquals(2L, dto.getCategoryId());
        assertEquals("Rice", dto.getCategoryName());
        assertEquals(1L, dto.getRestaurantId());
        assertEquals("Tapro Bistro", dto.getRestaurantName());
    }
}
