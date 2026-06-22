package qr_ordering_system.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    private Long id;
    private Long tenantId;
    private Long restaurantId;
    private String tableNumber;
    private String status;
    private Double totalAmount;
    private String restaurantCurrencyCode;
    private LocalDateTime orderTime;
    private List<OrderItemResponseDTO> items;

    // Constructors
    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Long id,
                            Long tenantId,
                            Long restaurantId,
                            String tableNumber,
                            String status,
                            Double totalAmount,
                            String restaurantCurrencyCode,
                            LocalDateTime orderTime,
                            List<OrderItemResponseDTO> items) {
        this.id = id;
        this.tenantId = tenantId;
        this.restaurantId = restaurantId;
        this.tableNumber = tableNumber;
        this.status = status;
        this.totalAmount = totalAmount;
        this.restaurantCurrencyCode = restaurantCurrencyCode;
        this.orderTime = orderTime;
        this.items = items;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRestaurantCurrencyCode() {
        return restaurantCurrencyCode;
    }

    public void setRestaurantCurrencyCode(String restaurantCurrencyCode) {
        this.restaurantCurrencyCode = restaurantCurrencyCode;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public List<OrderItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponseDTO> items) {
        this.items = items;
    }
}
