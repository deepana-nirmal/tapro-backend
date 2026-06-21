package qr_ordering_system.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.service.CategoryService;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void anonymousUserCanGetPublicRestaurantCategories() throws Exception {
        CategoryResponseDTO category = new CategoryResponseDTO();
        category.setId(2L);
        category.setName("Rice");
        category.setRestaurantId(1L);
        category.setVisible(true);
        category.setMenuItemCount(4L);

        when(categoryService.getPublicCategoriesByRestaurant(1L)).thenReturn(List.of(category));

        mockMvc.perform(get("/api/categories/public/restaurant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Rice"))
                .andExpect(jsonPath("$.data[0].menuItemCount").value(4));
    }
}
