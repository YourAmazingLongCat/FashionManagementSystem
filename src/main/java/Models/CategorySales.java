package Models;

import java.math.BigDecimal;

public class CategorySales {

    private String categoryId;
    private String categoryName;
    private int totalSold;
    private BigDecimal totalRevenue;

    public CategorySales() {}

    public CategorySales(String categoryId, String categoryName,
                         int totalSold, BigDecimal totalRevenue) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
