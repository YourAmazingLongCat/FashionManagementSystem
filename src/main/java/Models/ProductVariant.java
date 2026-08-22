package Models;

import java.math.BigDecimal;

public class ProductVariant {

    private String variantId;
    private String productId;
    private String sizeId;
    private String sizeName;
    private String colorId;
    private String colorName;
    private String colorHexCode;
    private String sku;
    private String imageUrl;
    private String productName;
    private String productDescription;
    private BigDecimal productBasePrice;
    private String categoryName;
    private int stockQty;
    private int reservedQty;
    private BigDecimal priceOverride;

    public ProductVariant() {
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSizeId() {
        return sizeId;
    }

    public void setSizeId(String sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHexCode() {
        return colorHexCode;
    }

    public void setColorHexCode(String colorHexCode) {
        this.colorHexCode = colorHexCode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public BigDecimal getProductBasePrice() { return productBasePrice; }
    public void setProductBasePrice(BigDecimal productBasePrice) { this.productBasePrice = productBasePrice; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /** Total physical stock, including units currently reserved by Pending orders. */
    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = Math.max(0, stockQty);
    }

    public int getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(int reservedQty) {
        this.reservedQty = Math.max(0, reservedQty);
    }

    /** Quantity that another customer may currently buy. */
    public int getAvailableQty() {
        return Math.max(0, stockQty - reservedQty);
    }

    public BigDecimal getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(BigDecimal priceOverride) {
        this.priceOverride = priceOverride;
    }
}
