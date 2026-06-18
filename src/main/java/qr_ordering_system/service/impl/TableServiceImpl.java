package qr_ordering_system.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import qr_ordering_system.dto.TableRequestDTO;
import qr_ordering_system.dto.TableResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantTable;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.RestaurantTableRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.QRCodeService;
import qr_ordering_system.service.TableService;

@Service
@Transactional
public class TableServiceImpl implements TableService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;

    public TableServiceImpl(RestaurantRepository restaurantRepository,
                            RestaurantTableRepository tableRepository,
                            UserRepository userRepository,
                            QRCodeService qrCodeService) {
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public TableResponseDTO createTable(TableRequestDTO dto, String currentUserEmail) {

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        authorizeRestaurantAccess(currentUserEmail, restaurant.getId());
        validateTableNumber(dto.getTableNumber(), restaurant.getId(), null);

        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(dto.getTableNumber().trim());
        table.setRestaurant(restaurant);

        RestaurantTable saved = tableRepository.save(table);
        saved.setQrCodeUrl(qrCodeService.buildMenuUrl(restaurant.getId(), String.valueOf(saved.getId())));

        tableRepository.save(saved);

        return mapToDTO(saved);
    }

    @Override
    public TableResponseDTO updateTable(Long tableId, TableRequestDTO dto, String currentUserEmail) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        authorizeRestaurantAccess(currentUserEmail, table.getRestaurant().getId());

        if (!table.getRestaurant().getId().equals(dto.getRestaurantId())) {
            throw new BadRequestException("Table restaurant cannot be changed");
        }

        validateTableNumber(dto.getTableNumber(), table.getRestaurant().getId(), tableId);

        table.setTableNumber(dto.getTableNumber().trim());
        return mapToDTO(tableRepository.save(table));
    }

    @Override
    public void deleteTable(Long tableId, String currentUserEmail) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        authorizeRestaurantAccess(currentUserEmail, table.getRestaurant().getId());
        tableRepository.delete(table);
    }

    @Override
    public List<TableResponseDTO> getRestaurantTables(Long restaurantId, String currentUserEmail) {
        authorizeRestaurantAccess(currentUserEmail, restaurantId);
        return tableRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TableResponseDTO getPublicTable(Long restaurantId, Long tableId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        if (!table.isActive()) {
            throw new BadRequestException("Table is inactive");
        }

        return mapToDTO(table);
    }

    private TableResponseDTO mapToDTO(RestaurantTable table) {
        String menuUrl = table.getQrCodeUrl();
        if (menuUrl == null || !menuUrl.contains("/table/")) {
            menuUrl = qrCodeService.buildMenuUrl(
                    table.getRestaurant() != null ? table.getRestaurant().getId() : null,
                    String.valueOf(table.getId())
            );
            table.setQrCodeUrl(menuUrl);
        }

        return new TableResponseDTO(
                table.getId(),
                table.getRestaurant() != null ? table.getRestaurant().getId() : null,
                table.getTableNumber(),
                menuUrl,
                qrCodeService.buildTableQrImageUrl(table.getId()),
                table.isActive()
        );
    }

    private void authorizeRestaurantAccess(String currentUserEmail, Long restaurantId) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only owners can manage tables");
        }

        if (user.getRestaurant() == null || !restaurantId.equals(user.getRestaurant().getId())) {
            throw new AccessDeniedException("Owners can manage only tables in their assigned restaurant");
        }
    }

    private void validateTableNumber(String tableNumber, Long restaurantId, Long tableId) {
        String normalized = tableNumber == null ? "" : tableNumber.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException("Table number is required");
        }

        boolean exists = tableId == null
                ? tableRepository.existsByRestaurantIdAndTableNumberIgnoreCase(restaurantId, normalized)
                : tableRepository.existsByRestaurantIdAndTableNumberIgnoreCaseAndIdNot(restaurantId, normalized, tableId);

        if (exists) {
            throw new BadRequestException("A table with this number already exists");
        }
    }
}
