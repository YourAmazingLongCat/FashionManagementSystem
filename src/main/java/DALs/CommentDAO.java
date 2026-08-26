package DALs;

import Models.Comment;
import Utils.DBContext;
import Utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Comment Management Module.
 * Supports:
 * - Creating product comments (review eligibility check)
 * - Viewing product comments (active comments for customers, all for staff/admin)
 * - Hiding / Unhiding comments (status toggling between 'Active' and 'Hidden')
 * - Searching comments by keyword (content, username, product name)
 * - Calculating product rating summary (average score, total count)
 * 
 * @author ngocpace191049-cmyk
 */
public class CommentDAO {

    public static final long EDIT_LIMIT_MS = 7L * 24 * 60 * 60 * 1000;
    public static final int ADD_LIMIT_DAYS = 7;

    private static final String BASE_SELECT
            = "SELECT c.commentId, c.variantId AS orderItemId, c.customerId AS accountId, c.rating, c.content, c.createdAt, c.status, "
            + "cu.fullName AS accountFullName, cu.username AS accountUsername, "
            + "p.productId, p.name AS productName, sz.sizeName, col.colorName "
            + "FROM Comments c "
            + "JOIN Customers cu ON c.customerId = cu.customerId "
            + "JOIN ProductVariants pv ON c.variantId = pv.variantId "
            + "JOIN Products p ON pv.productId = p.productId "
            + "JOIN Sizes sz ON pv.sizeId = sz.sizeId "
            + "JOIN Colors col ON pv.colorId = col.colorId ";

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setCommentId(rs.getString("commentId"));
        comment.setOrderItemId(rs.getString("orderItemId"));
        comment.setAccountId(rs.getString("accountId"));
        comment.setRating(rs.getInt("rating"));
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("createdAt"));
        comment.setStatus(rs.getString("status"));
        comment.setAccountFullName(rs.getString("accountFullName"));
        comment.setAccountUsername(rs.getString("accountUsername"));
        comment.setProductId(rs.getString("productId"));
        comment.setProductName(rs.getString("productName"));
        comment.setSizeName(rs.getString("sizeName"));
        comment.setColorName(rs.getString("colorName"));
        return comment;
    }

    /**
     * Get all active comments for a specific product (Guest & Customer view).
     */
    public List<Comment> getActiveCommentsByProduct(String productId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.productId = ? AND c.status = 'Active' ORDER BY c.createdAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("getActiveCommentsByProduct error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get all comments for a product, including hidden ones (Staff & Admin view).
     */
    public List<Comment> getAllCommentsByProduct(String productId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.productId = ? ORDER BY c.createdAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("getAllCommentsByProduct error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get all comments in the system (Admin management view).
     */
    public List<Comment> getAllComments() {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.createdAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("getAllComments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Count total active comments for a given product.
     */
    public int countActiveCommentsByProduct(String productId) {
        String sql = "SELECT COUNT(*) FROM Comments c "
                   + "JOIN ProductVariants pv ON c.variantId = pv.variantId "
                   + "WHERE pv.productId = ? AND c.status = 'Active'";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("countActiveCommentsByProduct error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Search comments by keyword across product name, content, or customer name/username.
     */
    public List<Comment> searchComments(String keyword) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE c.content LIKE ? OR cu.fullName LIKE ? OR cu.username LIKE ? OR p.name LIKE ? "
                + "ORDER BY c.createdAt DESC";
        String pattern = "%" + keyword + "%";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("searchComments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get single comment by its ID.
     */
    public Comment getCommentById(String commentId) {
        String sql = BASE_SELECT + "WHERE c.commentId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("getCommentById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Add a new product comment / review.
     */
    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO Comments (commentId, variantId, customerId, rating, content, createdAt, status) "
                   + "VALUES (?, ?, ?, ?, ?, GETDATE(), 'Active')";
        String commentId = Utils.generateId("CMT");
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            ps.setString(2, comment.getOrderItemId()); // variantId
            ps.setString(3, comment.getAccountId());   // customerId
            ps.setInt(4, comment.getRating());
            ps.setString(5, comment.getContent());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("addComment error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update rating and content of an existing comment.
     */
    public boolean updateComment(String commentId, int rating, String content) {
        String sql = "UPDATE Comments SET rating = ?, content = ? WHERE commentId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, content);
            ps.setString(3, commentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("updateComment error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggle status between 'Active' and 'Hidden' (Hide comment feature).
     */
    public boolean toggleCommentStatus(String commentId) {
        String sql = "UPDATE Comments SET status = CASE WHEN status = 'Active' THEN 'Hidden' ELSE 'Active' END WHERE commentId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("toggleCommentStatus error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete comment by ID.
     */
    public boolean deleteComment(String commentId) {
        String sql = "DELETE FROM Comments WHERE commentId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("deleteComment error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a customer owns a specific comment.
     */
    public boolean isCommentOwner(String commentId, String customerId) {
        String sql = "SELECT 1 FROM Comments WHERE commentId = ? AND customerId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            ps.setString(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("isCommentOwner error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a customer has already submitted a comment for a given product.
     */
    public boolean hasCustomerCommentedOnProduct(String customerId, String productId) {
        if (customerId == null || productId == null) return false;
        String sql = "SELECT 1 FROM Comments c "
                   + "JOIN ProductVariants pv ON c.variantId = pv.variantId "
                   + "WHERE c.customerId = ? AND pv.productId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("hasCustomerCommentedOnProduct error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Find a variantId purchased by this customer in a delivered order that can be reviewed.
     * Returns null if customer has already commented on this product.
     */
    public String getEligibleOrderItemId(String customerId, String productId) {
        if (hasCustomerCommentedOnProduct(customerId, productId)) {
            return null;
        }

        String sql = "SELECT TOP 1 pv.variantId "
                   + "FROM OrderItems oi "
                   + "JOIN Orders o ON oi.orderId = o.orderId "
                   + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                   + "WHERE o.customerId = ? AND pv.productId = ? AND o.orderStatus = 'Delivered' "
                   + "ORDER BY o.placedAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("variantId");
                }
            }
        } catch (Exception e) {
            System.err.println("getEligibleOrderItemId error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Calculate remaining days within the 7-day review window after delivery.
     */
    public int getRemainingDaysToComment(String customerId, String productId) {
        String sql = "SELECT TOP 1 DATEDIFF(DAY, GETDATE(), DATEADD(DAY, ?, o.placedAt)) AS remainingDays "
                   + "FROM OrderItems oi "
                   + "JOIN Orders o ON oi.orderId = o.orderId "
                   + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                   + "WHERE o.customerId = ? AND pv.productId = ? AND o.orderStatus = 'Delivered' "
                   + "ORDER BY o.placedAt DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ADD_LIMIT_DAYS);
            ps.setString(2, customerId);
            ps.setString(3, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.max(0, rs.getInt("remainingDays"));
                }
            }
        } catch (Exception e) {
            System.err.println("getRemainingDaysToComment error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Check order item review eligibility status.
     */
    public EligibilityStatus checkOrderItemEligibility(String orderItemIdOrVariantId, String customerId) {
        EligibilityStatus status = new EligibilityStatus();
        String sql = "SELECT o.orderStatus, o.placedAt, pv.productId "
                   + "FROM OrderItems oi "
                   + "JOIN Orders o ON oi.orderId = o.orderId "
                   + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                   + "WHERE (oi.orderItemId = ? OR oi.variantId = ?) AND o.customerId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderItemIdOrVariantId);
            ps.setString(2, orderItemIdOrVariantId);
            ps.setString(3, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String orderStatus = rs.getString("orderStatus");
                    String productId = rs.getString("productId");

                    if (hasCustomerCommentedOnProduct(customerId, productId)) {
                        status.setEligible(false);
                        status.setAlreadyReviewed(true);
                        status.setReason("You have already reviewed this product.");
                        return status;
                    }

                    if ("Delivered".equalsIgnoreCase(orderStatus)) {
                        status.setEligible(true);
                    } else {
                        status.setEligible(false);
                        status.setReason("Review is only available for delivered orders.");
                    }
                } else {
                    status.setEligible(false);
                    status.setReason("You have not purchased this product.");
                }
            }
        } catch (Exception e) {
            status.setEligible(false);
            status.setReason("System error checking review eligibility.");
        }
        return status;
    }

    /**
     * Get rating summary for a single product [avgRating, totalReviews].
     */
    public double[] getRatingSummary(String productId) {
        String sql = "SELECT AVG(CAST(c.rating AS FLOAT)) AS avgRating, COUNT(c.commentId) AS totalCount "
                   + "FROM Comments c "
                   + "JOIN ProductVariants pv ON c.variantId = pv.variantId "
                   + "WHERE pv.productId = ? AND c.status = 'Active'";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avgRating");
                    int count = rs.getInt("totalCount");
                    return new double[]{rs.wasNull() ? 0.0 : avg, (double) count};
                }
            }
        } catch (Exception e) {
            System.err.println("getRatingSummary error: " + e.getMessage());
        }
        return new double[]{0.0, 0.0};
    }

    /**
     * Get rating summary map for a list of product IDs in one batch query.
     */
    public Map<String, double[]> getRatingSummaryMap(List<String> productIds) {
        Map<String, double[]> map = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return map;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT pv.productId, AVG(CAST(c.rating AS FLOAT)) AS avgRating, COUNT(c.commentId) AS totalCount "
                   + "FROM Comments c "
                   + "JOIN ProductVariants pv ON c.variantId = pv.variantId "
                   + "WHERE pv.productId IN (" + placeholders + ") AND c.status = 'Active' "
                   + "GROUP BY pv.productId";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                ps.setString(i + 1, productIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pid = rs.getString("productId");
                    double avg = rs.getDouble("avgRating");
                    int count = rs.getInt("totalCount");
                    map.put(pid, new double[]{rs.wasNull() ? 0.0 : avg, (double) count});
                }
            }
        } catch (Exception e) {
            System.err.println("getRatingSummaryMap error: " + e.getMessage());
        }
        return map;
    }

    /**
     * Eligibility status holder.
     */
    public static class EligibilityStatus {
        private boolean eligible = false;
        private boolean alreadyReviewed = false;
        private boolean windowExpired = false;
        private int remainingDays = 0;
        private String reason;

        public boolean isEligible() { return eligible; }
        public void setEligible(boolean eligible) { this.eligible = eligible; }
        public boolean isAlreadyReviewed() { return alreadyReviewed; }
        public void setAlreadyReviewed(boolean alreadyReviewed) { this.alreadyReviewed = alreadyReviewed; }
        public boolean isWindowExpired() { return windowExpired; }
        public void setWindowExpired(boolean windowExpired) { this.windowExpired = windowExpired; }
        public int getRemainingDays() { return remainingDays; }
        public void setRemainingDays(int remainingDays) { this.remainingDays = remainingDays; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
