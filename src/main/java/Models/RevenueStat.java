package Models;

import java.math.BigDecimal;

/**
 * Model đại diện cho 1 điểm dữ liệu trên biểu đồ tăng trưởng doanh thu
 * (dùng cho biểu đồ đường - line chart).
 *
 * periodLabel: nhãn hiển thị trên trục X, ví dụ "2026-07-09", "Tuần 27/2026",
 *              "2026-07", "2026" tùy theo periodType (day/week/month/year)
 * totalRevenue: tổng doanh thu (SUM totalAmount) trong khoảng đó,
 *               chỉ tính các bill có paymentStatus = 'Paid'
 * billCount: số lượng hóa đơn trong khoảng đó
 */
public class RevenueStat {

    private String periodLabel;
    private BigDecimal totalRevenue;
    private int billCount;

    public RevenueStat() {
    }

    public RevenueStat(String periodLabel, BigDecimal totalRevenue, int billCount) {
        this.periodLabel = periodLabel;
        this.totalRevenue = totalRevenue;
        this.billCount = billCount;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getBillCount() {
        return billCount;
    }

    public void setBillCount(int billCount) {
        this.billCount = billCount;
    }
}
