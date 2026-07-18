/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 *
 * @author Admin
 */

public class ProductSale {
    private String productId;
    private String productCode;
    private String productName;
    private double unitCost;
    private double unitPrice;
    private int quantitySold;
    private double revenue;
    private double profit;

    public ProductSale() {
    }

    public ProductSale(String productId, String productCode, String productName,
                       double unitCost, double unitPrice, int quantitySold,
                       double revenue, double profit) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.unitCost = unitCost;
        this.unitPrice = unitPrice;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
        this.profit = profit;
    }

    // Getters and setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}