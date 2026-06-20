package qr_ordering_system.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    @Value("${app.frontend-urls:http://localhost:3000,http://127.0.0.1:3000}")
    private String frontendUrls = "http://localhost:3000,http://127.0.0.1:3000";

    /**
     * Generate QR code image as PNG byte array
     */
    public byte[] generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    WIDTH,
                    HEIGHT,
                    hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR Code", e);
        }
    }

    /**
     * Build frontend menu URL (non-secure version)
     */
    public String buildMenuUrl(Long restaurantId, String tableId) {
        return getPrimaryFrontendUrl() + "/menu/" + restaurantId + "/table/" + tableId;
    }

    /**
     * Build secure QR URL using token (optional advanced approach)
     */
    public String buildSecureUrl(String qrToken) {
        return "https://tapro.com/m/" + qrToken;
    }

    public String buildTableQrImageUrl(Long tableId) {
        return "/api/qr/tables/" + tableId;
    }

    private String getPrimaryFrontendUrl() {
        if (frontendUrls == null || frontendUrls.isBlank()) {
            return "http://localhost:3000";
        }

        for (String origin : frontendUrls.split(",")) {
            String trimmedOrigin = origin.trim();
            if (!trimmedOrigin.isBlank()) {
                return trimmedOrigin;
            }
        }

        return "http://localhost:3000";
    }
}
