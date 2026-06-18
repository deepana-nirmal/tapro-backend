package qr_ordering_system.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.MenuItemRequestDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.mapper.MenuItemMapper;
import qr_ordering_system.model.Category;
import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.MenuItemFeaturedLabel;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.CategoryRepository;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.MenuItemImageService;
import qr_ordering_system.service.MenuItemService;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private static final Logger log = LoggerFactory.getLogger(MenuItemServiceImpl.class);

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemMapper mapper;
    private final UserRepository userRepository;
    private final MenuItemImageService menuItemImageService;

    @Override
    public MenuItemResponseDTO create(MenuItemRequestDTO dto, String currentUserEmail) {

        log.info("Creating menu item {}", dto.getName());

        Restaurant r = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        User user = getUser(currentUserEmail);
        authorizeRestaurantAccess(user, r.getId());

        Category c = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!c.getRestaurant().getId().equals(r.getId())) {
            throw new BadRequestException("Category does not belong to the selected restaurant");
        }

        MenuItem m = new MenuItem();
        applyUpdates(m, dto, r, c);

        return mapper.toDto(menuItemRepository.save(m));
    }

    @Override
    public MenuItemResponseDTO update(Long id, MenuItemRequestDTO dto, String currentUserEmail) {

        MenuItem m = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        User user = getUser(currentUserEmail);
        authorizeRestaurantAccess(user, m.getRestaurant().getId());

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        authorizeRestaurantAccess(user, restaurant.getId());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BadRequestException("Category does not belong to the selected restaurant");
        }

        applyUpdates(m, dto, restaurant, category);

        return mapper.toDto(menuItemRepository.save(m));
    }

    @Override
    public void delete(Long id, String currentUserEmail) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        authorizeRestaurantAccess(getUser(currentUserEmail), menuItem.getRestaurant().getId());
        menuItemRepository.delete(menuItem);
    }

    @Override
    public MenuItemResponseDTO getById(Long id) {
        return mapper.toDto(menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found")));
    }

    @Override
    public List<MenuItemResponseDTO> getAll() {
        return menuItemRepository.findAll().stream()
                .map(mapper::toDto).toList();
    }

    @Override
    public List<MenuItemResponseDTO> getRestaurantMenu(Long restaurantId) {
        return menuItemRepository.findVisibleCustomerMenuByRestaurantIdAndStatusIn(
                        restaurantId,
                        List.of(MenuItemStatus.AVAILABLE, MenuItemStatus.OUT_OF_STOCK))
                .stream().map(mapper::toDto).toList();
    }

    @Override
    public List<MenuItemResponseDTO> getRestaurantMenuForManagement(Long restaurantId, String currentUserEmail) {
        authorizeRestaurantAccess(getUser(currentUserEmail), restaurantId);
        return menuItemRepository.findByRestaurant_Id(restaurantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemResponseDTO updateStatus(Long id, MenuItemStatus status, String currentUserEmail) {
        MenuItem menuItem = getAuthorizedMenuItem(id, currentUserEmail);
        menuItem.setStatus(status);
        return mapper.toDto(menuItemRepository.save(menuItem));
    }

    @Override
    public MenuItemResponseDTO updateFeatured(Long id, boolean featured, MenuItemFeaturedLabel featuredLabel, String currentUserEmail) {
        MenuItem menuItem = getAuthorizedMenuItem(id, currentUserEmail);
        menuItem.setFeatured(featured);
        menuItem.setFeaturedLabel(resolveFeaturedLabel(featured, featuredLabel));
        return mapper.toDto(menuItemRepository.save(menuItem));
    }

    @Override
    public MenuItemResponseDTO uploadImage(Long id, MultipartFile file, String currentUserEmail) {
        MenuItem menuItem = getAuthorizedMenuItem(id, currentUserEmail);
        menuItem.setImageUrl(menuItemImageService.store(file));
        return mapper.toDto(menuItemRepository.save(menuItem));
    }

    private void applyUpdates(MenuItem menuItem, MenuItemRequestDTO dto, Restaurant restaurant, Category category) {
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setStatus(dto.getStatus());
        boolean featured = Boolean.TRUE.equals(dto.getFeatured());
        menuItem.setFeatured(featured);
        menuItem.setFeaturedLabel(resolveFeaturedLabel(featured, dto.getFeaturedLabel()));
        menuItem.setPreparationTime(dto.getPreparationTime());
        menuItem.setImageUrl(dto.getImageUrl());
        menuItem.setIngredients(dto.getIngredients() != null ? dto.getIngredients().stream().map(String::trim).filter(value -> !value.isEmpty()).toList() : List.of());
        menuItem.setAllergens(dto.getAllergens() != null ? dto.getAllergens().stream().map(String::trim).filter(value -> !value.isEmpty()).toList() : List.of());
        menuItem.setRestaurant(restaurant);
        menuItem.setCategory(category);
    }

    private MenuItem getAuthorizedMenuItem(Long id, String currentUserEmail) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        authorizeRestaurantAccess(getUser(currentUserEmail), menuItem.getRestaurant().getId());
        return menuItem;
    }

    private User getUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void authorizeRestaurantAccess(User user, Long restaurantId) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            return;
        }

        if (user.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only owners and super admins can manage menu items");
        }

        if (user.getRestaurant() == null || !restaurantId.equals(user.getRestaurant().getId())) {
            throw new AccessDeniedException("Owners can manage only their assigned restaurant");
        }
    }

    private MenuItemFeaturedLabel resolveFeaturedLabel(boolean featured, MenuItemFeaturedLabel featuredLabel) {
        if (!featured) {
            return null;
        }

        if (featuredLabel == null) {
            throw new BadRequestException("Featured label is required when the item is featured");
        }

        return featuredLabel;
    }
}
