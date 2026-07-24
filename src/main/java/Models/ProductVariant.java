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

    public int getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(int reservedQty) {
        this.reservedQty = reservedQty;
    }

    public int getAvailableQty() {
        return stockQty - reservedQty;
    }

    public BigDecimal getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(BigDecimal priceOverride) {
        this.priceOverride = priceOverride;
    }
}
