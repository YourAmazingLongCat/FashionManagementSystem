package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Model - OOP entity representing a product in FashionShopDB
 * @author ADMIN
 */
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

    public Product(String productId, String categoryId, String name, String description,
                   BigDecimal basePrice, String status) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.status = status;
    }

    public String getProductId() {
        return productId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public List<String> getSizeIds() {
        return sizeIds;
    }

    public List<String> getSizeNames() {
        return sizeNames;
    }

    public List<String> getColorIds() {
        return colorIds;
    }

    public List<String> getColorNames() {
        return colorNames;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public int getTotalStockQty() {
        return totalStockQty;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public void setSizeIds(List<String> sizeIds) {
        this.sizeIds = sizeIds != null ? sizeIds : new ArrayList<>();
    }

    public void setSizeNames(List<String> sizeNames) {
        this.sizeNames = sizeNames != null ? sizeNames : new ArrayList<>();
    }

    public void setColorIds(List<String> colorIds) {
        this.colorIds = colorIds != null ? colorIds : new ArrayList<>();
    }

    public void setColorNames(List<String> colorNames) {
        this.colorNames = colorNames != null ? colorNames : new ArrayList<>();
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants != null ? variants : new ArrayList<>();
    }

    public void setTotalStockQty(int totalStockQty) {
        this.totalStockQty = totalStockQty;
    }

    @Override
    public String toString() {
        return "Product{" + "productId=" + productId + ", name=" + name
                + ", basePrice=" + basePrice + ", status=" + status + '}';
    }
}
