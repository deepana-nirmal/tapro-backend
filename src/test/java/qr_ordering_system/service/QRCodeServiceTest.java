package qr_ordering_system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {

    private final QRCodeService qrCodeService = new QRCodeService();

    @Test
    void testBuildMenuUrl() {
        Long restaurantId = 5L;
        String tableId = "T1";

        String url = qrCodeService.buildMenuUrl(restaurantId, tableId);

        assertEquals("http://localhost:3000/menu/5/table/T1", url);
    }

    @Test
    void testGenerateQRCode_NotNull() {
        String text = "http://localhost:3000/menu/5/table/T1";

        byte[] qrImage = qrCodeService.generateQRCode(text);

        assertNotNull(qrImage);
        assertTrue(qrImage.length > 0);
    }
}
