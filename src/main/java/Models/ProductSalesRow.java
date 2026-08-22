package Models;

import java.math.BigDecimal;


public class ProductSalesRow {

    private String productId;
    private String productName;
    private int totalQuantity;
    private int paidQuantity;      // số lượng sản phẩm thuộc các bill đã Paid
    private int unpaidQuantity;    // số lượng sản phẩm thuộc các bill chưa Paid
    private BigDecimal totalRevenuePaid;
    private BigDecimal totalUnpaidAmount;

    public ProductSalesRow() {
    }

    public ProductSalesRow(String productId, String productName, int totalQuantity,
                            int paidQuantity, int unpaidQuantity,
                            BigDecimal totalRevenuePaid, BigDecimal totalUnpaidAmount) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.paidQuantity = paidQuantity;
        this.unpaidQuantity = unpaidQuantity;
        this.totalRevenuePaid = totalRevenuePaid;
        this.totalUnpaidAmount = totalUnpaidAmount;
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

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getPaidQuantity() {
        return paidQuantity;
    }

    public void setPaidQuantity(int paidQuantity) {
        this.paidQuantity = paidQuantity;
    }

    public int getUnpaidQuantity() {
        return unpaidQuantity;
    }

    public void setUnpaidQuantity(int unpaidQuantity) {
        this.unpaidQuantity = unpaidQuantity;
    }

    public BigDecimal getTotalRevenuePaid() {
        return totalRevenuePaid;
    }

    public void setTotalRevenuePaid(BigDecimal totalRevenuePaid) {
        this.totalRevenuePaid = totalRevenuePaid;
    }

    public BigDecimal getTotalUnpaidAmount() {
        return totalUnpaidAmount;
    }

    public void setTotalUnpaidAmount(BigDecimal totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }
}