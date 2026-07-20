package Models;

import java.math.BigDecimal;

/**
 * Model đại diện cho MỘT dòng trong phần "chi tiết hóa đơn" (bill detail).
 * Bảng Bills không có sẵn danh sách sản phẩm, nên chi tiết hóa đơn thực chất
 * là join OrderItems -> ProductVariants -> Products/Sizes/Colors theo orderId
 * của hóa đơn đó.
 */
public class BillOrderItem {

    private String orderItemId;
    private String variantId;
    private String productId;
    private String productName;
    private String imageUrl;
    private String sizeName;
    private String colorName;
    private String sku;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;

    public BillOrderItem() {
    }

    // ==== Getters & Setters ====
    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Thành tiền = (đơn giá * số lượng) - giảm giá
     */
    public BigDecimal getSubtotal() {
        BigDecimal unit = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        BigDecimal discount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        return unit.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
    }
}
