package qr_ordering_system.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import qr_ordering_system.dto.MenuItemRequestDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.model.Category;
import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.CategoryRepository;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.MenuItemImageService;
import qr_ordering_system.service.impl.MenuItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private qr_ordering_system.mapper.MenuItemMapper menuItemMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuItemImageService menuItemImageService;

    @InjectMocks
    private MenuItemServiceImpl service;

    @Test
    void shouldCreateMenuItem() {

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Category category = new Category();
        category.setId(1L);
        category.setRestaurant(restaurant);

        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setRole(Role.OWNER);
        owner.setRestaurant(restaurant);

        MenuItemRequestDTO dto = new MenuItemRequestDTO();
        dto.setName("Burger");
        dto.setPrice(1200.0);
        dto.setStatus(MenuItemStatus.AVAILABLE);
        dto.setFeatured(false);
        dto.setPreparationTime(15);
        dto.setRestaurantId(1L);
        dto.setCategoryId(1L);

        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(owner));

        when(menuItemRepository.save(any(MenuItem.class)))
                .thenAnswer(i -> {
                    MenuItem m = i.getArgument(0);
                    m.setId(10L);
                    return m;
                });

        when(menuItemMapper.toDto(any(MenuItem.class)))
                .thenAnswer(i -> {
                    MenuItem m = i.getArgument(0);
                    MenuItemResponseDTO r = new MenuItemResponseDTO();
                    r.setId(m.getId());
                    r.setName(m.getName());
                    r.setPrice(m.getPrice());
                    return r;
                });

        MenuItemResponseDTO result = service.create(dto, "owner@test.com");

        assertEquals("Burger", result.getName());
        assertEquals(1200.0, result.getPrice());

        verify(menuItemRepository, times(1))
                .save(any(MenuItem.class));
    }
}
