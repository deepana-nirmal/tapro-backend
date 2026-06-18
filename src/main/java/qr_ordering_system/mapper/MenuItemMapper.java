package qr_ordering_system.mapper;

import org.springframework.stereotype.Component;

import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.model.MenuItem;

@Component
public class MenuItemMapper {

    public MenuItemResponseDTO toDto(MenuItem item) {

        if (item == null) return null;

        MenuItemResponseDTO dto = new MenuItemResponseDTO();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setStatus(item.getStatus());
        dto.setFeatured(item.isFeatured());
        dto.setFeaturedLabel(item.getFeaturedLabel());
        dto.setPreparationTime(item.getPreparationTime());
        dto.setImageUrl(item.getImageUrl());
        dto.setIngredients(item.getIngredients());
        dto.setAllergens(item.getAllergens());

        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
        }

        if (item.getRestaurant() != null) {
            dto.setRestaurantId(item.getRestaurant().getId());
            dto.setRestaurantName(item.getRestaurant().getName());
        }

        return dto;
    }
}
