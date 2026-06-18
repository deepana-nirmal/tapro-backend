package qr_ordering_system.dto;

public class CategoryResponseDTO {

    private Long id;
    private String name;
    private Long restaurantId;
    private String imageUrl;
    private Boolean visible;
    private Long menuItemCount;

    public CategoryResponseDTO() {}

    public CategoryResponseDTO(Long id, String name, Long restaurantId, String imageUrl, Boolean visible, Long menuItemCount) {
        this.id = id;
        this.name = name;
        this.restaurantId = restaurantId;
        this.imageUrl = imageUrl;
        this.visible = visible;
        this.menuItemCount = menuItemCount;
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

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Long getMenuItemCount() {
        return menuItemCount;
    }

    public void setMenuItemCount(Long menuItemCount) {
        this.menuItemCount = menuItemCount;
    }
}
