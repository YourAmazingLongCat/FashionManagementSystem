package Models;

import java.sql.Timestamp;

/**
 * Model representing a Product Comment / Review.
 * 
 * @author ngocpace191049-cmyk
 */
public class Comment {

    private String commentId;
    private String orderItemId;   // Maps to variantId or orderItemId
    private String accountId;     // customerId
    private int rating;           // 1 to 5 stars
    private String content;       // Review text
    private Timestamp createdAt;
    private String status;        // Active or Hidden

    // Extra display fields populated via JOIN queries
    private String accountFullName;
    private String accountUsername;
    private String productId;
    private String productName;
    private String sizeName;
    private String colorName;

    public Comment() {
    }

    public Comment(String commentId, String orderItemId, String accountId, int rating, String content, Timestamp createdAt, String status) {
        this.commentId = commentId;
        this.orderItemId = orderItemId;
        this.accountId = accountId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccountFullName() {
        return accountFullName;
    }

    public void setAccountFullName(String accountFullName) {
        this.accountFullName = accountFullName;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
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

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getVariantInfo() {
        StringBuilder sb = new StringBuilder();
        if (sizeName != null && !sizeName.isBlank()) {
            sb.append("Size: ").append(sizeName);
        }
        if (colorName != null && !colorName.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Color: ").append(colorName);
        }
        return sb.toString();
    }
}