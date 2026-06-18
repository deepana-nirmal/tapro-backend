package qr_ordering_system.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import qr_ordering_system.dto.OrderItemDTO;
import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void testCreateOrder() {

        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(1L);
        request.setTableNumber("T1");

        OrderItemDTO item = new OrderItemDTO();
        item.setMenuItemId(1L);
        item.setQuantity(2);

        request.setItems(java.util.List.of(item));

        OrderResponseDTO responseDTO = new OrderResponseDTO();

        when(orderService.createOrder(any())).thenReturn(responseDTO);

        ResponseEntity<?> response = orderController.createOrder(request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
}