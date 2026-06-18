package qr_ordering_system.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import qr_ordering_system.dto.CategoryRequestDTO;
import qr_ordering_system.dto.CategoryResponseDTO;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO dto, String currentUserEmail);

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto, String currentUserEmail);

    List<CategoryResponseDTO> getAllCategories();

    List<CategoryResponseDTO> getCategoriesByRestaurant(Long restaurantId, String currentUserEmail);

    CategoryResponseDTO getById(Long id);

    void delete(Long id, String currentUserEmail);

    CategoryResponseDTO updateVisibility(Long id, boolean visible, String currentUserEmail);

    CategoryResponseDTO uploadImage(Long categoryId, MultipartFile file, String currentUserEmail);

    CategoryResponseDTO uploadImageForRestaurant(Long restaurantId, Long categoryId, MultipartFile file);
}
