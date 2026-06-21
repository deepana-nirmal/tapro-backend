package qr_ordering_system.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import qr_ordering_system.dto.CategoryRequestDTO;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.Category;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.CategoryRepository;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.CategoryImageService;
import qr_ordering_system.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryImageService categoryImageService;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            MenuItemRepository menuItemRepository,
            CategoryImageService categoryImageService
    ) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
        this.categoryImageService = categoryImageService;
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto, String currentUserEmail) {
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        authorizeRestaurantAccess(currentUserEmail, restaurant.getId());

        Category category = new Category();
        category.setName(dto.getName());
        category.setRestaurant(restaurant);
        category.setImageUrl(dto.getImageUrl());
        category.setVisible(dto.getVisible() == null || dto.getVisible());

        Category saved = categoryRepository.save(category);
        return mapToDTO(saved);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto, String currentUserEmail) {
        Category category = getAuthorizedCategory(id, currentUserEmail);

        if (dto.getRestaurantId() != null && !dto.getRestaurantId().equals(category.getRestaurant().getId())) {
            throw new AccessDeniedException("Category restaurant cannot be changed");
        }

        category.setName(dto.getName());
        if (dto.getImageUrl() != null) {
            category.setImageUrl(dto.getImageUrl());
        }
        if (dto.getVisible() != null) {
            category.setVisible(dto.getVisible());
        }

        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> getCategoriesByRestaurant(Long restaurantId, String currentUserEmail) {
        authorizeRestaurantAccess(currentUserEmail, restaurantId);

        return categoryRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> getPublicCategoriesByRestaurant(Long restaurantId) {
        return categoryRepository.findByRestaurantIdAndVisibleTrue(restaurantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToDTO(category);
    }

    @Override
    public void delete(Long id, String currentUserEmail) {
        categoryRepository.delete(getAuthorizedCategory(id, currentUserEmail));
    }

    @Override
    public CategoryResponseDTO updateVisibility(Long id, boolean visible, String currentUserEmail) {
        Category category = getAuthorizedCategory(id, currentUserEmail);
        category.setVisible(visible);
        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDTO uploadImage(Long categoryId, MultipartFile file, String currentUserEmail) {
        Category category = getAuthorizedCategory(categoryId, currentUserEmail);
        category.setImageUrl(categoryImageService.store(category.getRestaurant().getId(), file));
        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDTO uploadImageForRestaurant(Long restaurantId, Long categoryId, MultipartFile file) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!restaurantId.equals(category.getRestaurant().getId())) {
            throw new AccessDeniedException("Category does not belong to the selected restaurant");
        }

        category.setImageUrl(categoryImageService.store(restaurantId, file));
        return mapToDTO(categoryRepository.save(category));
    }

    private CategoryResponseDTO mapToDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setRestaurantId(category.getRestaurant().getId());
        dto.setImageUrl(category.getImageUrl());
        dto.setVisible(category.isVisible());
        dto.setMenuItemCount(menuItemRepository.countByCategory_Id(category.getId()));
        return dto;
    }

    private Category getAuthorizedCategory(Long categoryId, String currentUserEmail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        authorizeRestaurantAccess(currentUserEmail, category.getRestaurant().getId());
        return category;
    }

    private void authorizeRestaurantAccess(String currentUserEmail, Long restaurantId) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.SUPER_ADMIN) {
            return;
        }

        if (user.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only owners and super admins can manage categories");
        }

        if (user.getRestaurant() == null || !restaurantId.equals(user.getRestaurant().getId())) {
            throw new AccessDeniedException("Owners can manage only their assigned restaurant");
        }
    }
}
