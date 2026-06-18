package qr_ordering_system.service;

import qr_ordering_system.dto.TableRequestDTO;
import qr_ordering_system.dto.TableResponseDTO;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantTable;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.RestaurantTableRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.QRCodeService;
import qr_ordering_system.service.impl.TableServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private TableServiceImpl tableService;

    private Restaurant restaurant;
    private User owner;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setRole(Role.OWNER);
        owner.setRestaurant(restaurant);
    }

    @Test
    void testCreateTable_success() {

        TableRequestDTO request = new TableRequestDTO();
        request.setRestaurantId(1L);
        request.setTableNumber("T1");

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setTableNumber("T1");
        table.setRestaurant(restaurant);

        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(owner));

        when(tableRepository.existsByRestaurantIdAndTableNumberIgnoreCase(1L, "T1"))
                .thenReturn(false);

        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> {
                    RestaurantTable saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(1L);
                    }
                    return saved;
                });

        when(qrCodeService.buildMenuUrl(1L, "1"))
                .thenReturn("http://localhost:3000/menu/1/table/1");

        when(qrCodeService.buildTableQrImageUrl(1L))
                .thenReturn("/api/qr/tables/1");

        TableResponseDTO response = tableService.createTable(request, "owner@test.com");

        assertNotNull(response);
        assertEquals("T1", response.getTableNumber());
    }

    @Test
    void testGetRestaurantTables() {

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setTableNumber("T1");
        table.setRestaurant(restaurant);

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(owner));

        when(tableRepository.findByRestaurantId(1L))
                .thenReturn(List.of(table));

        when(qrCodeService.buildMenuUrl(1L, "1"))
                .thenReturn("http://localhost:3000/menu/1/table/1");

        when(qrCodeService.buildTableQrImageUrl(1L))
                .thenReturn("/api/qr/tables/1");

        List<TableResponseDTO> result =
                tableService.getRestaurantTables(1L, "owner@test.com");

        assertEquals(1, result.size());
    }
}
