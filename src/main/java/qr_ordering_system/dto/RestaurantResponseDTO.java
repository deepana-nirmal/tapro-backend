package qr_ordering_system.dto;

import qr_ordering_system.model.RestaurantStatus;

public class RestaurantResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String logoUrl;
    private String description;
    private String openingHours;
    private Double serviceChargePercentage;
    private Double taxPercentage;
    private String currency;
    private String themeColor;
    private RestaurantStatus status;
    private Long activeOrderCount;
    private Double todayRevenue;

    public RestaurantResponseDTO() {
    }

    public RestaurantResponseDTO(
            Long id,
            String name,
            String address,
            String phone,
            String email,
            String logoUrl,
            String description,
            String openingHours,
            Double serviceChargePercentage,
            Double taxPercentage,
            String currency,
            String themeColor,
            RestaurantStatus status,
            Long activeOrderCount,
            Double todayRevenue) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.logoUrl = logoUrl;
        this.description = description;
        this.openingHours = openingHours;
        this.serviceChargePercentage = serviceChargePercentage;
        this.taxPercentage = taxPercentage;
        this.currency = currency;
        this.themeColor = themeColor;
        this.status = status;
        this.activeOrderCount = activeOrderCount;
        this.todayRevenue = todayRevenue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public Double getServiceChargePercentage() {
        return serviceChargePercentage;
    }

    public void setServiceChargePercentage(Double serviceChargePercentage) {
        this.serviceChargePercentage = serviceChargePercentage;
    }

    public Double getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(Double taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public void setStatus(RestaurantStatus status) {
        this.status = status;
    }

    public Long getActiveOrderCount() {
        return activeOrderCount;
    }

    public void setActiveOrderCount(Long activeOrderCount) {
        this.activeOrderCount = activeOrderCount;
    }

    public Double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(Double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }
}
