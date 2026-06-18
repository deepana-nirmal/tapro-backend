package qr_ordering_system.controller;

import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.RestaurantTable;
import qr_ordering_system.repository.RestaurantTableRepository;
import qr_ordering_system.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final RestaurantTableRepository tableRepository;

    public QRCodeController(QRCodeService qrCodeService, RestaurantTableRepository tableRepository) {
        this.qrCodeService = qrCodeService;
        this.tableRepository = tableRepository;
    }

    /**
     * Generate QR code using restaurantId + tableId
     * Example:
     * GET /api/qr/5/T1
     */
    @GetMapping(value = "/{restaurantId}/{tableId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQRCode(
            @PathVariable Long restaurantId,
            @PathVariable String tableId
    ) {

        String url = qrCodeService.buildMenuUrl(restaurantId, tableId);
        byte[] qrImage = qrCodeService.generateQRCode(url);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=table-qr.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    /**
     * OPTIONAL: Secure QR using token
     * Example:
     * GET /api/qr/token/abc123xyz
     */
    @GetMapping(value = "/token/{qrToken}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQRCodeByToken(
            @PathVariable String qrToken
    ) {

        String url = qrCodeService.buildSecureUrl(qrToken);
        byte[] qrImage = qrCodeService.generateQRCode(url);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=table-qr.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @GetMapping(value = "/tables/{tableId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateTableQRCode(@PathVariable Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        byte[] qrImage = qrCodeService.generateQRCode(table.getQrCodeUrl());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=table-" + tableId + "-qr.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}
