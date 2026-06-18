package qr_ordering_system.dto;

public class TableResponseDTO {

    private Long id;
    private Long restaurantId;
    private String tableNumber;
    private String qrCodeUrl;
    private String qrImageUrl;
    private boolean active;

    public TableResponseDTO(Long id, Long restaurantId, String tableNumber, String qrCodeUrl, String qrImageUrl, boolean active) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.tableNumber = tableNumber;
        this.qrCodeUrl = qrCodeUrl;
        this.qrImageUrl = qrImageUrl;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public String getQrImageUrl() {
        return qrImageUrl;
    }

    public boolean isActive() {
        return active;
    }
}
