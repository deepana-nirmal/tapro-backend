package qr_ordering_system.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import qr_ordering_system.dto.SuperAdminUserRequestDTO;
import qr_ordering_system.dto.SuperAdminUserResponseDTO;
import qr_ordering_system.dto.UsersByRestaurantResponseDTO;
import qr_ordering_system.model.Role;
import qr_ordering_system.service.AdminService;

@SpringBootTest
@AutoConfigureMockMvc
class SuperAdminUserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanGetUsers() throws Exception {
        SuperAdminUserResponseDTO user = new SuperAdminUserResponseDTO();
        user.setId(1L);
        user.setName("Owner One");
        user.setEmail("owner@example.com");
        user.setRole(Role.OWNER);
        user.setRestaurantId(10L);
        user.setRestaurantName("KFC Colombo");
        user.setEnabled(true);

        when(adminService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/super-admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("owner@example.com"));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void ownerCannotGetUsers() throws Exception {
        mockMvc.perform(get("/api/super-admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanGetUsersByRestaurantThroughCompatibilityEndpoint() throws Exception {
        UsersByRestaurantResponseDTO group = new UsersByRestaurantResponseDTO(10L, "KFC Colombo");

        when(adminService.getUsersByRestaurant()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/users/by-restaurant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].restaurantName").value("KFC Colombo"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotCreateUsers() throws Exception {
        SuperAdminUserRequestDTO request = new SuperAdminUserRequestDTO();
        request.setName("Staff User");
        request.setEmail("staff@example.com");
        request.setPassword("secret123");
        request.setRole(Role.STAFF);
        request.setRestaurantId(1L);
        request.setEnabled(true);

        mockMvc.perform(post("/api/super-admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
