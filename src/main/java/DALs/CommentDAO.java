package DALs;

import Models.Comment;
import Utils.DBContext;
import Utils.Utils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CommentDAO {

    // Giới hạn chỉnh sửa: 7 ngày (tính bằng milliseconds)
    public static final long EDIT_LIMIT_MS = 7L * 24 * 60 * 60 * 1000;

    // Giới hạn thêm comment mới: 7 ngày sau khi đơn hàng được giao
    public static final int ADD_LIMIT_DAYS = 7;

    private static final String BASE_SELECT
            = "SELECT c.commentId, c.orderItemId, c.accountId, c.rating, c.content, c.createdAt, c.status, "
            + "a.fullName AS accountFullName, a.username AS accountUsername, "
            + "p.productId, p.name AS productName, sz.sizeName, col.colorName "
            + "FROM Comments c "
            + "JOIN Accounts a ON c.accountId = a.accountId "
            + "JOIN OrderItems oi ON c.orderItemId = oi.orderItemId "
            + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
            + "JOIN Products p ON pv.productId = p.productId "
            + "JOIN Sizes sz ON pv.sizeId = sz.sizeId "
            + "JOIN Colors col ON pv.colorId = col.colorId ";

    // ==================== READ ====================
    public List<Comment> getActiveCommentsByProduct(String productId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.productId = ? AND c.status = 'Active' ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return list;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return list;
    }

    public List<Comment> getAllCommentsByProduct(String productId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.productId = ? ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return list;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return list;
    }

    // ==================== SEARCH ====================
    public List<Comment> searchComments(String keyword) {
        List<Comment> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllComments();
        }
        String sql = BASE_SELECT 
                + "WHERE c.content LIKE ? OR a.fullName LIKE ? OR a.username LIKE ? OR p.name LIKE ? "
                + "ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return list;
            }
            String likePattern = "%" + keyword.trim() + "%";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);
            ps.setString(4, likePattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return list;
    }

    public List<Comment> getAllComments() {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return list;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return list;
    }

    public Comment getCommentById(String commentId) {
        String sql = BASE_SELECT + "WHERE c.commentId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return null;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapComment(rs);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return null;
    }

    public List<Comment> getCommentsByAccount(String accountId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.accountId = ? ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return list;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComment(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return list;
    }

    // ==================== CREATE ====================
    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO Comments (commentId, orderItemId, accountId, rating, content, createdAt, status) "
                + "VALUES (?, ?, ?, ?, ?, GETDATE(), 'Active')";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return false;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, Utils.generateId("CMT"));
            ps.setString(2, comment.getOrderItemId());
            ps.setString(3, comment.getAccountId());
            ps.setInt(4, comment.getRating());
            ps.setString(5, comment.getContent());
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    // ==================== UPDATE ====================
    public boolean updateComment(String commentId, int rating, String content) {
        String sql = "UPDATE Comments SET rating = ?, content = ? WHERE commentId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return false;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, rating);
            ps.setString(2, content);
            ps.setString(3, commentId);
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public boolean toggleCommentStatus(String commentId) {
        String sql = "UPDATE Comments SET status = CASE WHEN status = 'Active' THEN 'Hidden' ELSE 'Active' END WHERE commentId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return false;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    // ==================== DELETE ====================
    public boolean deleteComment(String commentId) {
        String sql = "DELETE FROM Comments WHERE commentId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return false;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    // ==================== PERMISSION CHECKS ====================
    public String getEligibleOrderItemId(String accountId, String productId) {
        String sql
                = "SELECT oi.orderItemId, o.placedAt "
                + "FROM OrderItems oi "
                + "JOIN Orders o ON oi.orderId = o.orderId "
                + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "WHERE o.customerId = ? "
                + "AND pv.productId = ? "
                + "AND o.orderStatus = 'Delivered' "
                + "AND DATEDIFF(DAY, o.placedAt, GETDATE()) <= ? "
                + "AND oi.orderItemId NOT IN (SELECT orderItemId FROM Comments WHERE accountId = ?)";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return null;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ps.setString(2, productId);
            ps.setInt(3, ADD_LIMIT_DAYS);
            ps.setString(4, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("orderItemId");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return null;
    }

    public int getRemainingDaysToComment(String accountId, String productId) {
        String sql
                = "SELECT TOP 1 DATEDIFF(DAY, o.placedAt, GETDATE()) AS daysPassed "
                + "FROM OrderItems oi "
                + "JOIN Orders o ON oi.orderId = o.orderId "
                + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "WHERE o.customerId = ? "
                + "AND pv.productId = ? "
                + "AND o.orderStatus = 'Delivered' "
                + "AND oi.orderItemId NOT IN (SELECT orderItemId FROM Comments WHERE accountId = ?)";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return -1;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ps.setString(2, productId);
            ps.setString(3, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int daysPassed = rs.getInt("daysPassed");
                return Math.max(0, ADD_LIMIT_DAYS - daysPassed);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return -1;
    }

    /**
     * Kiểm tra eligibility cho một orderItem cụ thể
     */
    public EligibilityStatus checkOrderItemEligibility(String orderItemId, String accountId) {
        String sql
                = "SELECT o.orderStatus, o.placedAt, o.customerId, "
                + "(SELECT COUNT(*) FROM Comments WHERE orderItemId = ?) AS commentCount "
                + "FROM OrderItems oi "
                + "JOIN Orders o ON oi.orderId = o.orderId "
                + "WHERE oi.orderItemId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return EligibilityStatus.notEligible("Cannot connect to database");
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, orderItemId);
            ps.setString(2, orderItemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String orderStatus = rs.getString("orderStatus");
                Timestamp placedAt = rs.getTimestamp("placedAt");
                String customerId = rs.getString("customerId");
                int commentCount = rs.getInt("commentCount");

                // Check if belongs to this customer
                if (!accountId.equals(customerId)) {
                    return EligibilityStatus.notEligible("This order does not belong to you");
                }

                // Check if already reviewed
                if (commentCount > 0) {
                    return EligibilityStatus.alreadyReviewed("You have already reviewed this product");
                }

                // Check if delivered
                if (!"Delivered".equalsIgnoreCase(orderStatus)) {
                    return EligibilityStatus.notEligible("Order must be delivered before reviewing");
                }

                // Check 7-day window
                if (placedAt != null) {
                    long daysPassed = (System.currentTimeMillis() - placedAt.getTime()) / (24 * 60 * 60 * 1000);
                    if (daysPassed > ADD_LIMIT_DAYS) {
                        return EligibilityStatus.windowExpired("Review window has expired (7 days limit)");
                    }
                    return EligibilityStatus.eligible((int) (ADD_LIMIT_DAYS - daysPassed));
                }

                return EligibilityStatus.eligible(ADD_LIMIT_DAYS);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return EligibilityStatus.notEligible("Order item not found");
    }

    /**
     * Lấy comment của customer cho một orderItem cụ thể
     */
    public Comment getCommentByOrderItem(String orderItemId, String accountId) {
        String sql = BASE_SELECT + "WHERE c.orderItemId = ? AND c.accountId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return null;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, orderItemId);
            ps.setString(2, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapComment(rs);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return null;
    }

    public boolean isCommentOwner(String commentId, String accountId) {
        String sql = "SELECT COUNT(*) FROM Comments WHERE commentId = ? AND accountId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return false;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            ps.setString(2, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    /**
     * Kiểm tra comment còn trong thời hạn chỉnh sửa 7 ngày không
     * Chỉ áp dụng cho Admin/Staff, KHÔNG cho customer
     */
    public boolean isWithinEditLimit(String commentId) {
        // Customer reviews from order detail KHONG duoc sua/xoa
        // Chi Admin/Staff moi co the sua trong 7 ngay
        return false;
    }

    public double getAvgRatingByProduct(String productId) {
        // Tính cả comment bị ẩn - rating không thay đổi khi ẩn
        String sql
                = "SELECT AVG(CAST(c.rating AS FLOAT)) "
                + "FROM Comments c "
                + "JOIN OrderItems oi ON c.orderItemId = oi.orderItemId "
                + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "WHERE pv.productId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) {
                return 0.0;
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return 0.0;
    }

    /**
     * Lấy rating trung bình + tổng số đánh giá của 1 sản phẩm Trả về double[] {
     * avgRating, totalCount }
     */
/**
 * Lấy rating trung bình + tổng số đánh giá của 1 sản phẩm
 * Tính CẢ comment Hidden (chỉ ẩn nội dung bình luận, rating vẫn được tính)
 */
public double[] getRatingSummary(String productId) {
    String sql =
        "SELECT AVG(CAST(c.rating AS FLOAT)) AS avgRating, COUNT(*) AS totalCount " +
        "FROM Comments c " +
        "JOIN OrderItems oi ON c.orderItemId = oi.orderItemId " +
        "JOIN ProductVariants pv ON oi.variantId = pv.variantId " +
        "WHERE pv.productId = ?";
    Connection conn = null;
    try {
        conn = new DBContext().getConnection();
        if (conn == null) return new double[]{0, 0};
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, productId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            double avg = rs.getDouble("avgRating");
            int count = rs.getInt("totalCount");
            return new double[]{avg, count};
        }
        rs.close(); ps.close();
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        if (conn != null) try { conn.close(); } catch (SQLException e) {}
    }
    return new double[]{0, 0};
}

/**
 * Lấy rating cho NHIỀU sản phẩm cùng lúc (trang chủ / related products)
 * Tính CẢ comment Hidden
 */
public Map<String, double[]> getRatingSummaryMap(List<String> productIds) {
    Map<String, double[]> result = new HashMap<>();
    if (productIds == null || productIds.isEmpty()) return result;

    StringBuilder placeholders = new StringBuilder();
    for (int i = 0; i < productIds.size(); i++) {
        placeholders.append(i > 0 ? ",?" : "?");
    }

    String sql =
        "SELECT pv.productId, AVG(CAST(c.rating AS FLOAT)) AS avgRating, COUNT(*) AS totalCount " +
        "FROM Comments c " +
        "JOIN OrderItems oi ON c.orderItemId = oi.orderItemId " +
        "JOIN ProductVariants pv ON oi.variantId = pv.variantId " +
        "WHERE pv.productId IN (" + placeholders + ") " +
        "GROUP BY pv.productId";

    Connection conn = null;
    try {
        conn = new DBContext().getConnection();
        if (conn == null) return result;
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < productIds.size(); i++) {
            ps.setString(i + 1, productIds.get(i));
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String pid = rs.getString("productId");
            double avg = rs.getDouble("avgRating");
            int count = rs.getInt("totalCount");
            result.put(pid, new double[]{avg, count});
        }
        rs.close(); ps.close();
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        if (conn != null) try { conn.close(); } catch (SQLException e) {}
    }
    return result;
}
    // ==================== HELPER ====================

    private Comment mapComment(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setCommentId(rs.getString("commentId"));
        c.setOrderItemId(rs.getString("orderItemId"));
        c.setAccountId(rs.getString("accountId"));
        c.setRating(rs.getInt("rating"));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getTimestamp("createdAt"));
        c.setStatus(rs.getString("status"));
        c.setAccountFullName(rs.getString("accountFullName"));
        c.setAccountUsername(rs.getString("accountUsername"));
        c.setProductId(rs.getString("productId"));
        c.setProductName(rs.getString("productName"));
        c.setVariantInfo(rs.getString("sizeName") + " - " + rs.getString("colorName"));
        return c;
    }

    /**
     * Inner class for eligibility status
     */
    public static class EligibilityStatus {
        private final boolean eligible;
        private final boolean alreadyReviewed;
        private final boolean windowExpired;
        private final int remainingDays;
        private final String reason;

        private EligibilityStatus(boolean eligible, boolean alreadyReviewed, boolean windowExpired,
                int remainingDays, String reason) {
            this.eligible = eligible;
            this.alreadyReviewed = alreadyReviewed;
            this.windowExpired = windowExpired;
            this.remainingDays = remainingDays;
            this.reason = reason;
        }

        public static EligibilityStatus eligible(int remainingDays) {
            return new EligibilityStatus(true, false, false, remainingDays, null);
        }

        public static EligibilityStatus notEligible(String reason) {
            return new EligibilityStatus(false, false, false, 0, reason);
        }

        public static EligibilityStatus alreadyReviewed(String reason) {
            return new EligibilityStatus(false, true, false, 0, reason);
        }

        public static EligibilityStatus windowExpired(String reason) {
            return new EligibilityStatus(false, false, true, 0, reason);
        }

        public boolean isEligible() { return eligible; }
        public boolean isAlreadyReviewed() { return alreadyReviewed; }
        public boolean isWindowExpired() { return windowExpired; }
        public int getRemainingDays() { return remainingDays; }
        public String getReason() { return reason; }
    }
}
