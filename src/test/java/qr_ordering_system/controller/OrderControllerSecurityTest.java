package qr_ordering_system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import qr_ordering_system.dto.OrderItemDTO;
import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.service.OrderService;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void anonymousUserCanCreateOrder() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(4L);
        response.setTenantId(1L);
        response.setRestaurantId(1L);
        response.setTableNumber("A1");
        response.setStatus("PENDING");

        when(orderService.createOrder(any())).thenReturn(response);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(1L);
        request.setTableNumber("A1");
        OrderItemDTO item = new OrderItemDTO();
        item.setMenuItemId(1L);
        item.setQuantity(1);
        request.setItems(java.util.List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(4))
                .andExpect(jsonPath("$.data.restaurantId").value(1));
    }

    @Test
    void invalidAuthorizationHeaderDoesNotBlockPublicOrderCreate() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(5L);
        response.setTenantId(1L);
        response.setRestaurantId(1L);
        response.setTableNumber("A1");
        response.setStatus("PENDING");

        when(orderService.createOrder(any())).thenReturn(response);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(1L);
        request.setTableNumber("A1");
        OrderItemDTO item = new OrderItemDTO();
        item.setMenuItemId(1L);
        item.setQuantity(1);
        request.setItems(java.util.List.of(item));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void anonymousUserCanTrackPublicOrder() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(4L);
        response.setTenantId(1L);
        response.setRestaurantId(1L);
        response.setTableNumber("A1");
        response.setStatus("PENDING");

        when(orderService.getPublicOrderById(1L, 4L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/public/4").param("restaurantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(4))
                .andExpect(jsonPath("$.data.restaurantId").value(1));
    }

    @Test
    void invalidAuthorizationHeaderDoesNotBlockPublicOrderTracking() throws Exception {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(6L);
        response.setTenantId(1L);
        response.setRestaurantId(1L);
        response.setTableNumber("A1");
        response.setStatus("PENDING");

        when(orderService.getPublicOrderById(1L, 6L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/public/6")
                        .header("Authorization", "Bearer invalid-token")
                        .param("restaurantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(6));
    }
}
