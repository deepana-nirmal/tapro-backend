package qr_ordering_system.service;

import qr_ordering_system.dto.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface RestaurantService {

    RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto);

    List<RestaurantResponseDTO> getAllRestaurants();

    RestaurantResponseDTO getById(Long id);

    RestaurantResponseDTO getPublicById(Long id);

    RestaurantResponseDTO updateRestaurant(Long id, RestaurantRequestDTO dto, String currentUserEmail);

    RestaurantResponseDTO uploadLogo(Long restaurantId, MultipartFile file, String currentUserEmail);

    RestaurantResponseDTO uploadLogoAsOwner(String currentUserEmail, MultipartFile file);

    RestaurantResponseDTO activateRestaurant(Long id);

    RestaurantResponseDTO suspendRestaurant(Long id);

    void delete(Long id);
}
