package qr_ordering_system.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import qr_ordering_system.dto.CategoryRequestDTO;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.model.Category;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.CategoryRepository;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.CategoryImageService;
import qr_ordering_system.service.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CategoryImageService categoryImageService;

    @InjectMocks
    private CategoryServiceImpl service;

    @Test
    void createCategory_success() {
        Restaurant r = new Restaurant();
        r.setId(1L);
        r.setName("R");
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(r));
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setRole(Role.OWNER);
        owner.setRestaurant(r);
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        Category saved = new Category();
        saved.setId(2L);
        saved.setName("Drinks");
        saved.setRestaurant(r);
        saved.setVisible(true);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(menuItemRepository.countByCategory_Id(2L)).thenReturn(0L);

        CategoryRequestDTO req = new CategoryRequestDTO();
        req.setName("Drinks");
        req.setRestaurantId(1L);

        CategoryResponseDTO resp = service.createCategory(req, "owner@test.com");

        assertNotNull(resp);
        assertEquals(2L, resp.getId());
        assertEquals("Drinks", resp.getName());
        assertEquals(1L, resp.getRestaurantId());
    }

    @Test
    void getById_found() {
        Restaurant r = new Restaurant();
        r.setId(1L);
        r.setName("R");
        Category c = new Category();
        c.setId(10L);
        c.setName("Food");
        c.setRestaurant(r);
        c.setVisible(true);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(c));
        when(menuItemRepository.countByCategory_Id(10L)).thenReturn(0L);

        CategoryResponseDTO dto = service.getById(10L);

        assertEquals(10L, dto.getId());
        assertEquals("Food", dto.getName());
        assertEquals(1L, dto.getRestaurantId());
    }

    @Test
    void getById_notFound_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(99L));
    }

    @Test
    void getAllCategories_mapsCorrectly() {
        Restaurant r = new Restaurant();
        r.setId(1L);
        r.setName("R");
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("A");
        c1.setRestaurant(r);
        c1.setVisible(true);
        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("B");
        c2.setRestaurant(r);
        c2.setVisible(true);
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));
        when(menuItemRepository.countByCategory_Id(1L)).thenReturn(0L);
        when(menuItemRepository.countByCategory_Id(2L)).thenReturn(0L);

        List<CategoryResponseDTO> list = service.getAllCategories();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getName());
    }

    @Test
    void getCategoriesByRestaurant_filtersCorrectly() {
        Restaurant r = new Restaurant();
        r.setId(5L);
        r.setName("R");
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setRole(Role.OWNER);
        owner.setRestaurant(r);
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        Category c = new Category();
        c.setId(3L);
        c.setName("X");
        c.setRestaurant(r);
        c.setVisible(true);
        when(categoryRepository.findByRestaurantId(5L)).thenReturn(List.of(c));
        when(menuItemRepository.countByCategory_Id(3L)).thenReturn(0L);

        List<CategoryResponseDTO> res = service.getCategoriesByRestaurant(5L, "owner@test.com");
        assertEquals(1, res.size());
        assertEquals(5L, res.get(0).getRestaurantId());
    }

    @Test
    void getPublicCategoriesByRestaurant_returnsVisibleCategoriesOnly() {
        Restaurant r = new Restaurant();
        r.setId(7L);
        r.setName("R");

        Category visible = new Category();
        visible.setId(11L);
        visible.setName("Desserts");
        visible.setRestaurant(r);
        visible.setVisible(true);

        when(categoryRepository.findByRestaurantIdAndVisibleTrue(7L)).thenReturn(List.of(visible));
        when(menuItemRepository.countByCategory_Id(11L)).thenReturn(2L);

        List<CategoryResponseDTO> res = service.getPublicCategoriesByRestaurant(7L);

        assertEquals(1, res.size());
        assertEquals("Desserts", res.get(0).getName());
        assertEquals(2L, res.get(0).getMenuItemCount());
    }
}
