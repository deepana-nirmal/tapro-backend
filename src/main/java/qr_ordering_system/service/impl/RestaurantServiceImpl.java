package qr_ordering_system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.RestaurantRequestDTO;
import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.RestaurantLogoService;
import qr_ordering_system.service.RestaurantService;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.PREPARING,
            OrderStatus.READY
    );
    private static final List<OrderStatus> COMPLETED_ORDER_STATUSES = List.of(OrderStatus.COMPLETED);

    private final RestaurantRepository repository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RestaurantLogoService restaurantLogoService;

    @Override
    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto) {

        log.info("Creating restaurant {}", dto.getName());

        Restaurant r = new Restaurant();
        r.setName(dto.getName());
        r.setAddress(dto.getAddress());
        r.setPhone(dto.getPhone());
        r.setEmail(dto.getEmail());
        r.setLogoUrl(dto.getLogoUrl());
        r.setDescription(dto.getDescription());
        r.setOpeningHours(dto.getOpeningHours());
        r.setServiceChargePercentage(dto.getServiceChargePercentage());
        r.setTaxPercentage(dto.getTaxPercentage());
        r.setCurrency(dto.getCurrency());
        r.setThemeColor(dto.getThemeColor());
        r.setStatus(RestaurantStatus.ACTIVE);

        return mapToDTO(repository.save(r));
    }

    @Override
    public List<RestaurantResponseDTO> getAllRestaurants() {
        List<Restaurant> restaurants = repository.findAll();
        if (restaurants.isEmpty()) {
            return List.of();
        }

        List<Long> tenantIds = restaurants.stream()
                .map(Restaurant::getId)
                .toList();
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        Map<Long, Long> activeOrderCounts = orderRepository.countOrdersByTenantIdsAndStatusesAsMap(
                tenantIds,
                ACTIVE_ORDER_STATUSES
        );
        Map<Long, Double> todayRevenueByTenant = orderRepository.sumRevenueByTenantIdsAndStatusesAndCreatedAtBetweenAsMap(
                tenantIds,
                COMPLETED_ORDER_STATUSES,
                todayStart,
                tomorrowStart
        );

        return restaurants.stream()
                .map(restaurant -> mapToDTO(
                        restaurant,
                        activeOrderCounts.getOrDefault(restaurant.getId(), 0L),
                        todayRevenueByTenant.getOrDefault(restaurant.getId(), 0D)
                ))
                .toList();
    }

    @Override
    public RestaurantResponseDTO getById(Long id) {

        Restaurant r = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        return mapToDTO(r);
    }

    @Override
    public RestaurantResponseDTO getPublicById(Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        return mapToDTO(restaurant);
    }

    @Override
    public RestaurantResponseDTO updateRestaurant(Long id, RestaurantRequestDTO dto, String currentUserEmail) {

        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        authorizeRestaurantUpdate(id, currentUserEmail);

        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setLogoUrl(dto.getLogoUrl());
        restaurant.setDescription(dto.getDescription());
        restaurant.setOpeningHours(dto.getOpeningHours());
        restaurant.setServiceChargePercentage(dto.getServiceChargePercentage());
        restaurant.setTaxPercentage(dto.getTaxPercentage());
        restaurant.setCurrency(dto.getCurrency());
        restaurant.setThemeColor(dto.getThemeColor());

        return mapToDTO(repository.save(restaurant));
    }

    @Override
    public RestaurantResponseDTO uploadLogo(Long restaurantId, MultipartFile file, String currentUserEmail) {
        Restaurant restaurant = repository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        authorizeRestaurantUpdate(restaurantId, currentUserEmail);
        restaurant.setLogoUrl(restaurantLogoService.store(restaurantId, file));
        return mapToDTO(repository.save(restaurant));
    }

    @Override
    public RestaurantResponseDTO uploadLogoAsOwner(String currentUserEmail, MultipartFile file) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only owners can upload restaurant logos here");
        }

        if (user.getRestaurant() == null) {
            throw new AccessDeniedException("Owner is not assigned to a restaurant");
        }

        return uploadLogo(user.getRestaurant().getId(), file, currentUserEmail);
    }

    @Override
    public void delete(Long id) {

        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        repository.delete(restaurant);
    }

    @Override
    public RestaurantResponseDTO activateRestaurant(Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setStatus(RestaurantStatus.ACTIVE);
        return mapToDTO(repository.save(restaurant));
    }

    @Override
    public RestaurantResponseDTO suspendRestaurant(Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setStatus(RestaurantStatus.SUSPENDED);
        return mapToDTO(repository.save(restaurant));
    }

    private RestaurantResponseDTO mapToDTO(Restaurant r) {
        long activeOrderCount = orderRepository.findByTenantIdAndStatusIn(
                r.getId(),
                ACTIVE_ORDER_STATUSES
        ).size();
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();

        double todayRevenue = orderRepository.findOrdersForRestaurant(
                        r.getId(),
                        COMPLETED_ORDER_STATUSES,
                        todayStart,
                        null
                )
                .stream()
                .mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount() : 0D)
                .sum();

        return mapToDTO(r, activeOrderCount, todayRevenue);
    }

    private RestaurantResponseDTO mapToDTO(Restaurant r, long activeOrderCount, double todayRevenue) {
        return new RestaurantResponseDTO(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getPhone(),
                r.getEmail(),
                r.getLogoUrl(),
                r.getDescription(),
                r.getOpeningHours(),
                r.getServiceChargePercentage(),
                r.getTaxPercentage(),
                r.getCurrency(),
                r.getThemeColor(),
                r.getStatus(),
                activeOrderCount,
                todayRevenue
        );
    }

    private void authorizeRestaurantUpdate(Long restaurantId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.OWNER) {
            if (user.getRestaurant() == null) {
                throw new AccessDeniedException("Owner is not assigned to a restaurant");
            }

            if (!user.getRestaurant().getId().equals(restaurantId)) {
                throw new AccessDeniedException("Owners can edit only their assigned restaurant");
            }
            return;
        }

        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only owners and super admins can update restaurant settings");
        }
    }
}
