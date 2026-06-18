package qr_ordering_system.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import qr_ordering_system.model.MenuItemFeaturedLabel;
import qr_ordering_system.model.MenuItemStatus;

public class MenuItemRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Status is required")
    private MenuItemStatus status;

    @NotNull(message = "Featured flag is required")
    private Boolean featured;

    private MenuItemFeaturedLabel featuredLabel;

    @NotNull(message = "Preparation time is required")
    @PositiveOrZero(message = "Preparation time must be 0 or greater")
    private Integer preparationTime;

    private String imageUrl;

    private List<@NotBlank(message = "Ingredients cannot be blank") @Size(max = 100, message = "Ingredient must be 100 characters or fewer") String> ingredients;

    private List<@NotBlank(message = "Allergens cannot be blank") @Size(max = 100, message = "Allergen must be 100 characters or fewer") String> allergens;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    public MenuItemRequestDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public MenuItemStatus getStatus() {
        return status;
    }

    public void setStatus(MenuItemStatus status) {
        this.status = status;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public MenuItemFeaturedLabel getFeaturedLabel() {
        return featuredLabel;
    }

    public void setFeaturedLabel(MenuItemFeaturedLabel featuredLabel) {
        this.featuredLabel = featuredLabel;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(Integer preparationTime) {
        this.preparationTime = preparationTime;
    }

    @AssertTrue(message = "Featured label is required when the item is featured")
    public boolean isFeaturedLabelValid() {
        return !Boolean.TRUE.equals(featured) || featuredLabel != null;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
