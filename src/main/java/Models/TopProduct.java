package Models;

import java.math.BigDecimal;

public class TopProduct {

    private String productId;
    private String productName;
    private String imageUrl;
    private int totalSold;
    private int totalRevenue;

    public TopProduct() {}

    public TopProduct(String productId, String productName, String imageUrl,
                      int totalSold, int totalRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public int getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(int totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
