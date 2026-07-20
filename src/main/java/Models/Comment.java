package Models;

import java.util.Date;

public class Comment {
    private String commentId;
    private String orderItemId;
    private String accountId;
    private int rating;
    private String content;
    private Date createdAt;
    private String status; // "Active", "Hidden"

    // Extra fields for display (JOIN data)
    private String accountFullName;
    private String accountUsername;
    private String productName;
    private String productId;
    private String variantInfo; // size + color info

    public Comment() {}

    public Comment(String commentId, String orderItemId, String accountId,
                   int rating, String content, Date createdAt, String status) {
        this.commentId = commentId;
        this.orderItemId = orderItemId;
        this.accountId = accountId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getOrderItemId() { return orderItemId; }
    public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccountFullName() { return accountFullName; }
    public void setAccountFullName(String accountFullName) { this.accountFullName = accountFullName; }

    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getVariantInfo() { return variantInfo; }
    public void setVariantInfo(String variantInfo) { this.variantInfo = variantInfo; }

    public boolean isActive() { return "Active".equalsIgnoreCase(status); }
}