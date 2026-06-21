package qr_ordering_system.controller;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.service.CategoryService;
import qr_ordering_system.service.OwnerService;
import qr_ordering_system.service.RestaurantService;

class OwnerControllerTest {

    private final OwnerService ownerService = mock(OwnerService.class);
    private final CategoryService categoryService = mock(CategoryService.class);
    private final RestaurantService restaurantService = mock(RestaurantService.class);
    private final OwnerController ownerController = new OwnerController(ownerService, categoryService, restaurantService);

    @Test
    void testGetAllOrders() {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        dto.setStatus("PENDING");
        dto.setTenantId(1L);

        when(ownerService.getAllOrders(1L, "owner@demo.com"))
                .thenReturn(List.of(dto));

        var response = ownerController.getAllOrders(
                1L,
                new UsernamePasswordAuthenticationToken("owner@demo.com", "token")
        );

        assertEquals(1, response.getBody().size());
        assertEquals("PENDING", response.getBody().get(0).getStatus());
    }

    @Test
    void testGetByStatus() {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(2L);
        dto.setStatus("PENDING");
        dto.setTenantId(1L);

        when(ownerService.getOrdersByStatus(1L, OrderStatus.PENDING, "owner@demo.com"))
                .thenReturn(List.of(dto));

        var response = ownerController.getByStatus(
                1L,
                OrderStatus.PENDING,
                new UsernamePasswordAuthenticationToken("owner@demo.com", "token")
        );

        assertEquals(1, response.getBody().size());
        assertEquals("PENDING", response.getBody().get(0).getStatus());
    }

    @Test
    void testGetSingleOrder() {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(10L);
        dto.setStatus("PREPARING");
        dto.setTenantId(1L);

        when(ownerService.getOrder(10L, "owner@demo.com"))
                .thenReturn(dto);

        var response = ownerController.getOrder(
                10L,
                new UsernamePasswordAuthenticationToken("owner@demo.com", "token")
        );

        assertEquals(10L, response.getBody().getId());
        assertEquals("PREPARING", response.getBody().getStatus());
    }

    @Test
    void testGetActiveOrders() {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(20L);
        dto.setStatus("PENDING");
        dto.setTenantId(1L);

        when(ownerService.getActiveOrders("owner@demo.com"))
                .thenReturn(List.of(dto));

        var response = ownerController.getActiveOrders(
                new UsernamePasswordAuthenticationToken("owner@demo.com", "token")
        );

        assertEquals(1, response.getBody().size());
        assertEquals("PENDING", response.getBody().get(0).getStatus());
    }

    @Test
    void testGetPastOrders() {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(30L);
        dto.setStatus("COMPLETED");
        dto.setTenantId(1L);
        dto.setTableNumber("T9");

        when(ownerService.getPastOrders("owner@demo.com", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 21), OrderStatus.COMPLETED, "T9"))
                .thenReturn(List.of(dto));

        var response = ownerController.getPastOrders(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 21),
                OrderStatus.COMPLETED,
                "T9",
                new UsernamePasswordAuthenticationToken("owner@demo.com", "token")
        );

        assertEquals(1, response.getBody().size());
        assertEquals("T9", response.getBody().get(0).getTableNumber());
    }
}
