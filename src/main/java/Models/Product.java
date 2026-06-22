package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private String productId;
    private String categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String primaryImageUrl;
    private List<String> sizeIds = new ArrayList<>();
    private List<String> sizeNames = new ArrayList<>();
    private List<String> colorIds = new ArrayList<>();
    private List<String> colorNames = new ArrayList<>();
    private List<ProductVariant> variants = new ArrayList<>();
    private int totalStockQty;

    public Product() {
    }

    public Product(String productId, String categoryId, String name, BigDecimal basePrice, String status) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.name = name;
        this.basePrice = basePrice;
        this.status = status;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

    public List<String> getSizeIds() { return sizeIds; }
    public void setSizeIds(List<String> sizeIds) { this.sizeIds = sizeIds != null ? sizeIds : new ArrayList<>(); }

    public List<String> getSizeNames() { return sizeNames; }
    public void setSizeNames(List<String> sizeNames) { this.sizeNames = sizeNames != null ? sizeNames : new ArrayList<>(); }

    public List<String> getColorIds() { return colorIds; }
    public void setColorIds(List<String> colorIds) { this.colorIds = colorIds != null ? colorIds : new ArrayList<>(); }

    public List<String> getColorNames() { return colorNames; }
    public void setColorNames(List<String> colorNames) { this.colorNames = colorNames != null ? colorNames : new ArrayList<>(); }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants != null ? variants : new ArrayList<>(); }

    public int getTotalStockQty() { return totalStockQty; }
    public void setTotalStockQty(int totalStockQty) { this.totalStockQty = totalStockQty; }

    public static class Builder {
        private final Product product;

        public Builder() {
            this.product = new Product();
        }

        public Builder productId(String val) { product.productId = val; return this; }
        public Builder categoryId(String val) { product.categoryId = val; return this; }
        public Builder categoryName(String val) { product.categoryName = val; return this; }
        public Builder name(String val) { product.name = val; return this; }
        public Builder description(String val) { product.description = val; return this; }
        public Builder basePrice(BigDecimal val) { product.basePrice = val; return this; }
        public Builder status(String val) { product.status = val; return this; }
        public Builder createdAt(LocalDateTime val) { product.createdAt = val; return this; }
        public Builder updatedAt(LocalDateTime val) { product.updatedAt = val; return this; }
        public Builder primaryImageUrl(String val) { product.primaryImageUrl = val; return this; }
        public Builder variants(List<ProductVariant> val) { product.variants = val; return this; }
        public Builder totalStockQty(int val) { product.totalStockQty = val; return this; }

        public Product build() { return product; }
    }

    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_OUT_OF_STOCK = "OutOfStock";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final List<String> VALID_STATUSES = List.of(STATUS_AVAILABLE, STATUS_OUT_OF_STOCK, STATUS_INACTIVE);

    public boolean isValid() { return isValid(false); }

    public boolean isValid(boolean includeId) {
        if (includeId && isBlank(productId)) return false;
        if (isBlank(categoryId)) return false;
        if (isBlank(name)) return false;
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (!isValidStatus(status)) return false;
        return true;
    }

    public static boolean isValidStatus(String status) {
        return status != null && VALID_STATUSES.contains(status);
    }

    public boolean isAvailable() { return STATUS_AVAILABLE.equals(status); }
    public boolean isInStock() { return totalStockQty > 0; }
    public boolean hasVariants() { return variants != null && !variants.isEmpty(); }

    private boolean isBlank(String str) { return str == null || str.isBlank(); }

    public BigDecimal getDisplayPrice() {
        BigDecimal displayPrice = basePrice;
        if (hasVariants()) {
            for (ProductVariant variant : variants) {
                if (variant != null && variant.getPriceOverride() != null
                        && (displayPrice == null || variant.getPriceOverride().compareTo(displayPrice) < 0)) {
                    displayPrice = variant.getPriceOverride();
                }
            }
        }
        return displayPrice != null ? displayPrice : BigDecimal.ZERO;
    }

    public int getAvailableSizeCount() { return sizeIds != null ? sizeIds.size() : 0; }
    public int getAvailableColorCount() { return colorIds != null ? colorIds.size() : 0; }

    public String getFormattedPrice() {
        if (basePrice == null) return "0 đ";
        return String.format("%,.0f đ", basePrice);
    }

    @Override
    public String toString() {
        return "Product{productId=" + productId + ", name=" + name + ", basePrice=" + basePrice + ", status=" + status + '}';
    }
}
