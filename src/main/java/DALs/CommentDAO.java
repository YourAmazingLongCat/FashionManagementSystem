package DALs;

import Models.Comment;
import Utils.DBContext;
import Utils.Utils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    // Giới hạn chỉnh sửa: 7 ngày (tính bằng milliseconds)
    public static final long EDIT_LIMIT_MS = 1L * 60 * 1000;

    private static final String BASE_SELECT =
        "SELECT c.commentId, c.orderItemId, c.accountId, c.rating, c.content, c.createdAt, c.status, " +
        "a.fullName AS accountFullName, a.username AS accountUsername, " +
        "p.productId, p.name AS productName, sz.sizeName, col.colorName " +
        "FROM Comments c " +
        "JOIN Accounts a ON c.accountId = a.accountId " +
        "JOIN OrderItems oi ON c.orderItemId = oi.orderItemId " +
        "JOIN ProductVariants pv ON oi.variantId = pv.variantId " +
        "JOIN Products p ON pv.productId = p.productId " +
        "JOIN Sizes sz ON pv.sizeId = sz.sizeId " +
        "JOIN Colors col ON pv.colorId = col.colorId ";

    // ==================== READ ====================

    public List<Comment> getActiveCommentsByProduct(String productId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.productId = ? AND c.status = 'Active' ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection(); 
            if (conn == null) return list;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapComment(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return list;
    }

    public List<Comment> getAllCommentsByProduct(String productId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.productId = ? ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection();
            if (conn == null) return list;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapComment(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return list;
    }

    public List<Comment> getAllComments() {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
           conn = new DBContext().getConnection();
            if (conn == null) return list;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapComment(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return list;
    }

    public Comment getCommentById(String commentId) {
        String sql = BASE_SELECT + "WHERE c.commentId = ?";
        Connection conn = null;
        try {
       conn = new DBContext().getConnection();
            if (conn == null) return null;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapComment(rs);
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return null;
    }

    public List<Comment> getCommentsByAccount(String accountId) {
        List<Comment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.accountId = ? ORDER BY c.createdAt DESC";
        Connection conn = null;
        try {
       conn = new DBContext().getConnection();
            if (conn == null) return list;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapComment(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return list;
    }

    // ==================== CREATE ====================

    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO Comments (commentId, orderItemId, accountId, rating, content, createdAt, status) " +
                     "VALUES (?, ?, ?, ?, ?, GETDATE(), 'Active')";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection();
            if (conn == null) return false;
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
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    // ==================== UPDATE ====================

    public boolean updateComment(String commentId, int rating, String content) {
        String sql = "UPDATE Comments SET rating = ?, content = ? WHERE commentId = ?";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection();
            if (conn == null) return false;
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
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    public boolean toggleCommentStatus(String commentId) {
        String sql = "UPDATE Comments SET status = CASE WHEN status = 'Active' THEN 'Hidden' ELSE 'Active' END WHERE commentId = ?";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection();
            if (conn == null) return false;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    // ==================== DELETE ====================

    public boolean deleteComment(String commentId) {
        String sql = "DELETE FROM Comments WHERE commentId = ?";
        Connection conn = null;
        try {
         conn = new DBContext().getConnection();
            if (conn == null) return false;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    // ==================== PERMISSION CHECKS ====================

    public String getEligibleOrderItemId(String accountId, String productId) {
        String sql =
            "SELECT oi.orderItemId " +
            "FROM OrderItems oi " +
            "JOIN Orders o ON oi.orderId = o.orderId " +
            "JOIN ProductVariants pv ON oi.variantId = pv.variantId " +
            "WHERE o.customerId = ? " +
            "AND pv.productId = ? " +
            "AND o.orderStatus = 'Delivered' " +
            "AND oi.orderItemId NOT IN (SELECT orderItemId FROM Comments WHERE accountId = ?)";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) return null;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ps.setString(2, productId);
            ps.setString(3, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("orderItemId");
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return null;
    }

    public boolean isCommentOwner(String commentId, String accountId) {
        String sql = "SELECT COUNT(*) FROM Comments WHERE commentId = ? AND accountId = ?";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection();
            if (conn == null) return false;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            ps.setString(2, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    /**
     * Kiểm tra comment còn trong thời hạn chỉnh sửa 7 ngày không
     */
    public boolean isWithinEditLimit(String commentId) {
        String sql = "SELECT createdAt FROM Comments WHERE commentId = ?";
        Connection conn = null;
        try {
          conn = new DBContext().getConnection();
            if (conn == null) return false;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp createdAt = rs.getTimestamp("createdAt");
                if (createdAt != null) {
                    long diff = System.currentTimeMillis() - createdAt.getTime();
                    return diff <= EDIT_LIMIT_MS;
                }
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    public double getAvgRatingByProduct(String productId) {
        // Tính cả comment bị ẩn - rating không thay đổi khi ẩn
        String sql =
            "SELECT AVG(CAST(c.rating AS FLOAT)) " +
            "FROM Comments c " +
            "JOIN OrderItems oi ON c.orderItemId = oi.orderItemId " +
            "JOIN ProductVariants pv ON oi.variantId = pv.variantId " +
            "WHERE pv.productId = ?";
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            if (conn == null) return 0.0;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return 0.0;
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
}