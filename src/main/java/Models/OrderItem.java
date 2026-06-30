package Models;

import java.math.BigDecimal;

public class OrderItem {

    private String orderItemId;
    private String orderId;
    private String variantId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;

    public OrderItem() {
    }

    public OrderItem(String orderItemId, String orderId, String variantId,
            int quantity, BigDecimal unitPrice, BigDecimal discountAmount) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
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

    public BigDecimal getSubTotal() {
        BigDecimal price = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        BigDecimal discount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        return price.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
    }
}
