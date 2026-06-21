package qr_ordering_system.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.exception.GlobalExceptionHandler;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.service.RestaurantLogoService;
import qr_ordering_system.service.RestaurantService;

class RestaurantControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private RestaurantLogoService restaurantLogoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new RestaurantController(restaurantService, restaurantLogoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRestaurantByIdReturnsWrappedJson() throws Exception {
        RestaurantResponseDTO dto = new RestaurantResponseDTO();
        dto.setId(1L);
        dto.setName("Harbor Table");
        dto.setStatus(RestaurantStatus.ACTIVE);
        dto.setActiveOrderCount(2L);
        dto.setTodayRevenue(42.5);

        when(restaurantService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.activeOrderCount").value(2))
                .andExpect(jsonPath("$.data.todayRevenue").value(42.5));
    }

    @Test
    void getRestaurantByIdHidesRawSqlErrors() throws Exception {
        when(restaurantService.getById(1L))
                .thenThrow(new RuntimeException("ERROR: function lower(bytea) does not exist"));

        mockMvc.perform(get("/api/restaurants/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(content().string(not(containsString("lower(bytea)"))));
    }
}
