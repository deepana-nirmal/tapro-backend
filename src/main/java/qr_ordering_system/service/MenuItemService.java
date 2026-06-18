package qr_ordering_system.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import qr_ordering_system.dto.MenuItemRequestDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.model.MenuItemFeaturedLabel;
import qr_ordering_system.model.MenuItemStatus;

public interface MenuItemService {

    MenuItemResponseDTO create(MenuItemRequestDTO dto, String currentUserEmail);

    MenuItemResponseDTO update(Long id, MenuItemRequestDTO dto, String currentUserEmail);

    void delete(Long id, String currentUserEmail);

    MenuItemResponseDTO getById(Long id);

    List<MenuItemResponseDTO> getAll();

    List<MenuItemResponseDTO> getRestaurantMenu(Long restaurantId);

    List<MenuItemResponseDTO> getRestaurantMenuForManagement(Long restaurantId, String currentUserEmail);

    MenuItemResponseDTO updateStatus(Long id, MenuItemStatus status, String currentUserEmail);

    MenuItemResponseDTO updateFeatured(Long id, boolean featured, MenuItemFeaturedLabel featuredLabel, String currentUserEmail);

    MenuItemResponseDTO uploadImage(Long id, MultipartFile file, String currentUserEmail);
}
