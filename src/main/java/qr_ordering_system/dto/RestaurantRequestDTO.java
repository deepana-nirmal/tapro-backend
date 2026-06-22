package qr_ordering_system.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RestaurantRequestDTO {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone is required")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 500, message = "Logo URL must be 500 characters or fewer")
    private String logoUrl;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    @Size(max = 500, message = "Opening hours must be 500 characters or fewer")
    private String openingHours;

    @DecimalMin(value = "0.0", message = "Service charge must be at least 0")
    @DecimalMax(value = "100.0", message = "Service charge cannot exceed 100")
    private Double serviceChargePercentage;

    @DecimalMin(value = "0.0", message = "Tax must be at least 0")
    @DecimalMax(value = "100.0", message = "Tax cannot exceed 100")
    private Double taxPercentage;

    @JsonAlias("currency")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode;

    @NotBlank(message = "Theme color is required")
    @Pattern(
            regexp = "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$",
            message = "Theme color must be a valid hex color"
    )
    private String themeColor;

    public RestaurantRequestDTO() {}

    public RestaurantRequestDTO(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }
}
