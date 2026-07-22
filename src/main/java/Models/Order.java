package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {

    private String orderId;
    private String customerId;
    private String orderStatus;
    private String shippingAddress;
    private String phone;
    private LocalDateTime placedAt;
    private BigDecimal totalAmount;
    private boolean orderPlaced;

    public Order() {
    }

    public Order(String orderId, String customerId, String orderStatus,
            String shippingAddress, String phone, LocalDateTime placedAt,
            BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderStatus = orderStatus;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.placedAt = placedAt;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }

    public void setOrderPlaced(boolean orderPlaced) {
        this.orderPlaced = orderPlaced;
    }

    /**
     * An order that has not been placed must be completed within 2 days.
     * Once a Purchase payment record exists, the countdown no longer applies.
     */
    public LocalDateTime getConfirmationExpiresAt() {
        return placedAt == null || orderPlaced ? null : placedAt.plusDays(2);
    }

    public boolean isAwaitingConfirmation() {
        return "Pending".equalsIgnoreCase(orderStatus) && !orderPlaced;
    }

}
