package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpiredOrderInfo {

    private final String orderId;
    private final String customerId;
    private final String customerEmail;
    private final String customerName;
    private final LocalDateTime placedAt;
    private final BigDecimal totalAmount;

    public ExpiredOrderInfo(String orderId, String customerId, String customerEmail,
            String customerName, LocalDateTime placedAt, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.placedAt = placedAt;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
