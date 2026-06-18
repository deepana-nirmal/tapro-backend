package qr_ordering_system.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

import lombok.RequiredArgsConstructor;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.dto.CategoryRequestDTO;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.service.CategoryImageService;
import qr_ordering_system.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryImageService categoryImageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> create(@RequestBody CategoryRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Category created successfully",
                categoryService.createCategory(dto, authentication.getName())
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> update(
            @PathVariable Long id,
            @RequestBody CategoryRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Category updated successfully",
                categoryService.updateCategory(id, dto, authentication.getName())
        ));
    }

    @PutMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateVisibility(
            @PathVariable Long id,
            @RequestParam boolean visible,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                visible ? "Category shown successfully" : "Category hidden successfully",
                categoryService.updateVisibility(id, visible, authentication.getName())
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Categories retrieved successfully",
                categoryService.getAllCategories()
        ));
    }

    @GetMapping("/restaurant/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getByRestaurant(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant categories retrieved successfully",
                categoryService.getCategoriesByRestaurant(id, authentication.getName())
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Category retrieved successfully",
                categoryService.getById(id)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication authentication) {
        categoryService.delete(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Category deleted successfully", null));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Category image uploaded successfully",
                categoryService.uploadImage(id, file, authentication.getName())
        ));
    }

    @GetMapping("/images/{restaurantId}/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable Long restaurantId, @PathVariable String filename) throws IOException {
        Resource resource = categoryImageService.loadAsResource(restaurantId, filename);
        String mediaType = categoryImageService.detectContentType(restaurantId, filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .contentType(MediaType.parseMediaType(mediaType))
                .body(resource);
    }
}
