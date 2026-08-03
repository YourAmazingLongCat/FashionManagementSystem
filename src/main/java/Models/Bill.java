package Models;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model for the Bills table.
 * Besides raw table columns, this class also has joined fields
 * (customerName, customerPhone, shippingAddress, orderStatus, orderId...)
 * fetched from Orders/Accounts to display in the list view
 * without loading separate Order/Account objects.
 */
public class Bill {

    // ==== Original Bills columns ====
    private String billId;
    private String orderId;
    private String paymentMethod;
    private String paymentStatus;   // Pending | Paid | Failed | Refunded
    private Timestamp issuedDate;
    private BigDecimal totalAmount;

    // ==== Joined info (not in Bills table) ====
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String orderStatus;     // Pending | Confirmed | Processing | Shipping | Delivered | Cancelled
    private Timestamp placedAt;

    public Bill() {
    }

    public Bill(String billId, String orderId, String paymentMethod, String paymentStatus,
                Timestamp issuedDate, BigDecimal totalAmount) {
        this.billId = billId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.issuedDate = issuedDate;
        this.totalAmount = totalAmount;
    }

    // ==== Getters & Setters ====
    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Timestamp getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Timestamp issuedDate) {
        this.issuedDate = issuedDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Timestamp getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Timestamp placedAt) {
        this.placedAt = placedAt;
    }
}