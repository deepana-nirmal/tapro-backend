package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.RestaurantService;

@RestController
@RequestMapping("/api/super-admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminRestaurantsController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponseDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurants retrieved successfully",
                restaurantService.getAllRestaurants()
        ));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> getById(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant retrieved successfully",
                restaurantService.getById(restaurantId)
        ));
    }

    @PostMapping(value = "/{restaurantId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> uploadLogo(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant logo uploaded successfully",
                restaurantService.uploadLogo(restaurantId, file, authentication.getName())
        ));
    }
}
