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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.RestaurantRequestDTO;
import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.RestaurantLogoService;
import qr_ordering_system.service.RestaurantService;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Validated
public class RestaurantController {

    private final RestaurantService service;
    private final RestaurantLogoService restaurantLogoService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> create(
            @Valid @RequestBody RestaurantRequestDTO dto) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant created successfully",
                        service.createRestaurant(dto)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponseDTO>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurants retrieved successfully",
                        service.getAllRestaurants()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant retrieved successfully",
                        service.getById(id)
                )
        );
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> getPublicById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant retrieved successfully",
                        service.getPublicById(id)
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequestDTO dto,
            Authentication authentication) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant updated successfully",
                        service.updateRestaurant(id, dto, authentication.getName())
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant deleted successfully",
                        null
                )
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant activated successfully",
                        service.activateRestaurant(id)
                )
        );
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant suspended successfully",
                        service.suspendRestaurant(id)
                )
        );
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant logo uploaded successfully",
                        service.uploadLogo(id, file, authentication.getName())
                )
        );
    }

    @GetMapping("/logos/{restaurantId}/{filename:.+}")
    public ResponseEntity<Resource> getLogo(@PathVariable Long restaurantId, @PathVariable String filename) throws IOException {
        Resource resource = restaurantLogoService.loadAsResource(restaurantId, filename);
        String mediaType = restaurantLogoService.detectContentType(restaurantId, filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .contentType(MediaType.parseMediaType(mediaType))
                .body(resource);
    }
}
