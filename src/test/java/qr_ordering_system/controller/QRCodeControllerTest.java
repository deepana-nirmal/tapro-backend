package qr_ordering_system.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qr_ordering_system.service.QRCodeService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QRCodeControllerTest {

    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private QRCodeController qrCodeController;

    @Test
    void testGenerateQRCode() {

        when(qrCodeService.buildMenuUrl(5L, "T1"))
                .thenReturn("https://tapro.com/menu/5/T1");

        when(qrCodeService.generateQRCode(anyString()))
                .thenReturn("fake-image".getBytes());

        var response = qrCodeController.generateQRCode(5L, "T1");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }
}