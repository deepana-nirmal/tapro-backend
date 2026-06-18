package qr_ordering_system.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import qr_ordering_system.dto.MenuItemRequestDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.service.MenuItemImageService;
import qr_ordering_system.service.MenuItemService;

@ExtendWith(MockitoExtension.class)
class MenuItemControllerTest {

    @Mock
    private MenuItemService menuItemService;

    @Mock
    private MenuItemImageService menuItemImageService;

    @InjectMocks
    private MenuItemController controller;

    @Test
    void shouldGetAllMenuItems() {

        MenuItemResponseDTO dto1 = new MenuItemResponseDTO();
        dto1.setId(1L);

        MenuItemResponseDTO dto2 = new MenuItemResponseDTO();
        dto2.setId(2L);

        when(menuItemService.getAll())
                .thenReturn(List.of(dto1, dto2));

        var response = controller.getAll();

        assertEquals(2, response.getBody().getData().size());
    }

    @Test
    void shouldGetMenuItemById() {

        MenuItemResponseDTO dto = new MenuItemResponseDTO();
        dto.setId(1L);

        when(menuItemService.getById(1L)).thenReturn(dto);

        var response = controller.getById(1L);

        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void shouldGetRestaurantMenu() {

        MenuItemResponseDTO dto1 = new MenuItemResponseDTO();
        dto1.setId(1L);

        MenuItemResponseDTO dto2 = new MenuItemResponseDTO();
        dto2.setId(2L);

        when(menuItemService.getRestaurantMenu(1L))
                .thenReturn(List.of(dto1, dto2));

        var response = controller.getRestaurantMenu(1L);

        assertEquals(2, response.getBody().getData().size());
    }

    @Test
    void shouldCreateMenuItem() {

        MenuItemRequestDTO request = new MenuItemRequestDTO();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        MenuItemResponseDTO responseDTO = new MenuItemResponseDTO();
        responseDTO.setId(1L);

        when(authentication.getName()).thenReturn("owner@test.com");
        when(menuItemService.create(any(), eq("owner@test.com"))).thenReturn(responseDTO);

        var response = controller.create(request, authentication);

        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void shouldUpdateMenuItem() {

        MenuItemRequestDTO request = new MenuItemRequestDTO();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        MenuItemResponseDTO responseDTO = new MenuItemResponseDTO();
        responseDTO.setId(1L);

        when(authentication.getName()).thenReturn("owner@test.com");
        when(menuItemService.update(eq(1L), any(), eq("owner@test.com"))).thenReturn(responseDTO);

        var response = controller.update(1L, request, authentication);

        assertEquals(1L, response.getBody().getData().getId());
    }
}
