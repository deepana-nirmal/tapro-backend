package qr_ordering_system.service;

import java.util.List;

import qr_ordering_system.dto.TableRequestDTO;
import qr_ordering_system.dto.TableResponseDTO;

public interface TableService {

    TableResponseDTO createTable(TableRequestDTO dto, String currentUserEmail);

    TableResponseDTO updateTable(Long tableId, TableRequestDTO dto, String currentUserEmail);

    void deleteTable(Long tableId, String currentUserEmail);

    List<TableResponseDTO> getRestaurantTables(Long restaurantId, String currentUserEmail);

    TableResponseDTO getPublicTable(Long restaurantId, Long tableId);
}
