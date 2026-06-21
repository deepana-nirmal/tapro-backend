package qr_ordering_system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import qr_ordering_system.dto.DashboardMetricResponseDTO;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.RestaurantRequestDTO;
import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.model.Role;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.InvitationService;
import qr_ordering_system.service.RestaurantService;
import qr_ordering_system.service.SuperAdminDashboardService;

@SpringBootTest
@AutoConfigureMockMvc
class SuperAdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private InvitationService invitationService;

    @MockBean
    private SuperAdminDashboardService superAdminDashboardService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanReadDashboardMetrics() throws Exception {
        when(superAdminDashboardService.getMetrics()).thenReturn(List.of(
                new DashboardMetricResponseDTO("Total Restaurants", 2, "Active tenants across the platform", "blue")
        ));

        mockMvc.perform(get("/api/super-admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].label").value("Total Restaurants"));
    }

    @Test
    void anonymousCannotReadDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/super-admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void ownerCannotReadDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/super-admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN", username = "admin@tapro.com")
    void superAdminCanCreateRestaurantThroughSuperAdminEndpoint() throws Exception {
        RestaurantRequestDTO request = new RestaurantRequestDTO();
        request.setName("Demo Restaurant");
        request.setAddress("1 Main Street");
        request.setPhone("+1 555 555 5555");
        request.setEmail("demo@restaurant.com");
        request.setCurrency("USD");
        request.setThemeColor("#10b981");
        request.setServiceChargePercentage(0.0);
        request.setTaxPercentage(0.0);

        RestaurantResponseDTO response = new RestaurantResponseDTO();
        response.setId(11L);
        response.setName("Demo Restaurant");
        response.setAddress("1 Main Street");
        response.setPhone("+1 555 555 5555");
        response.setEmail("demo@restaurant.com");

        when(restaurantService.createRestaurant(any(RestaurantRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/super-admin/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Demo Restaurant"));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void ownerCannotCreateRestaurantThroughSuperAdminEndpoint() throws Exception {
        RestaurantRequestDTO request = new RestaurantRequestDTO();
        request.setName("Demo Restaurant");
        request.setAddress("1 Main Street");
        request.setPhone("+1 555 555 5555");
        request.setEmail("demo@restaurant.com");
        request.setCurrency("USD");
        request.setThemeColor("#10b981");
        request.setServiceChargePercentage(0.0);
        request.setTaxPercentage(0.0);

        mockMvc.perform(post("/api/super-admin/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanSendInvitation() throws Exception {
        InvitationRequest request = new InvitationRequest();
        request.setEmail("owner@restaurant.com");
        request.setRestaurantId(1L);
        request.setRole(Role.OWNER);

        mockMvc.perform(post("/api/invitations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanSuspendRestaurantThroughSuperAdminEndpoint() throws Exception {
        RestaurantResponseDTO response = new RestaurantResponseDTO();
        response.setId(7L);
        response.setName("Suspended Demo");

        when(restaurantService.suspendRestaurant(eq(7L))).thenReturn(response);

        mockMvc.perform(patch("/api/super-admin/restaurants/7/suspend").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(7));
    }
}
