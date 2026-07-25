package Models;

import java.math.BigDecimal;

public class PaymentMethodStats {

    private String paymentMethod;
    private int totalBills;
    private BigDecimal totalRevenue;

    public PaymentMethodStats() {}

    public PaymentMethodStats(String paymentMethod, int totalBills, BigDecimal totalRevenue) {
        this.paymentMethod = paymentMethod;
        this.totalBills = totalBills;
        this.totalRevenue = totalRevenue;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getTotalBills() {
        return totalBills;
    }

    public void setTotalBills(int totalBills) {
        this.totalBills = totalBills;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
