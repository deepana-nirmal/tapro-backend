package qr_ordering_system.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.MenuItemRequestDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.model.MenuItemFeaturedLabel;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.MenuItemImageService;
import qr_ordering_system.service.MenuItemService;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@Validated
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final MenuItemImageService menuItemImageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> create(
            @Valid @RequestBody MenuItemRequestDTO dto,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item created successfully",
                        menuItemService.create(dto, authentication.getName())
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequestDTO dto,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item updated successfully",
                        menuItemService.update(id, dto, authentication.getName())
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Authentication authentication) {

        menuItemService.delete(id, authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item deleted successfully",
                        null
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item retrieved successfully",
                        menuItemService.getById(id)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu items retrieved successfully",
                        menuItemService.getAll()
                )
        );
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getRestaurantMenu(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant menu retrieved successfully",
                        menuItemService.getRestaurantMenu(restaurantId)
                )
        );
    }

    @GetMapping("/restaurant/{restaurantId}/manage")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getRestaurantMenuForManagement(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant menu management data retrieved successfully",
                        menuItemService.getRestaurantMenuForManagement(restaurantId, authentication.getName())
                )
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam MenuItemStatus status,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item status updated successfully",
                        menuItemService.updateStatus(id, status, authentication.getName())
                )
        );
    }

    @PutMapping("/{id}/featured")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> updateFeatured(
            @PathVariable Long id,
            @RequestParam boolean featured,
            @RequestParam(required = false) MenuItemFeaturedLabel featuredLabel,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item featured status updated successfully",
                        menuItemService.updateFeatured(id, featured, featuredLabel, authentication.getName())
                )
        );
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Menu item image uploaded successfully",
                        menuItemService.uploadImage(id, file, authentication.getName())
                )
        );
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Resource resource = menuItemImageService.loadAsResource(filename);
        String mediaType = menuItemImageService.detectContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .contentType(MediaType.parseMediaType(mediaType))
                .body(resource);
    }
}
