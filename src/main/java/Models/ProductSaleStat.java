package Models;

import java.math.BigDecimal;


public class ProductSaleStat {

    private String periodLabel;
    private int quantitySold;
    private BigDecimal revenuePaid; // chỉ tính các bill có paymentStatus = 'Paid'

    public ProductSaleStat() {
    }

    public ProductSaleStat(String periodLabel, int quantitySold, BigDecimal revenuePaid) {
        this.periodLabel = periodLabel;
        this.quantitySold = quantitySold;
        this.revenuePaid = revenuePaid;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getRevenuePaid() {
        return revenuePaid;
    }

    public void setRevenuePaid(BigDecimal revenuePaid) {
        this.revenuePaid = revenuePaid;
    }
}
