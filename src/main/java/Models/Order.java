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
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private LocalDateTime issuedDate;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String cancelledBy;

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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }

    /**
     * Bill is merged into Order, so the invoice identifier is derived from
     * the order identifier instead of requiring a separate Bills row.
     */
    public String getInvoiceId() {
        return orderId == null || orderId.trim().isEmpty()
                ? null : "INV-" + orderId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
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

    public boolean isCancellable() {
        if (orderStatus == null || !orderPlaced) {
            return false;
        }
        String status = orderStatus.toLowerCase().trim();
        return "pending".equals(status) 
                || "confirmed".equals(status) 
                || "processing".equals(status);
    }

}
