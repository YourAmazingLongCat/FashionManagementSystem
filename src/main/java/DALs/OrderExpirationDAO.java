package DALs;

import Models.ExpiredOrderInfo;
import Utils.DBContext;
import Utils.OrderStatus;
import Utils.PaymentType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Removes checkout records that were never placed within two days.
 */
public class OrderExpirationDAO {

    public List<ExpiredOrderInfo> getExpiredPendingOrders() {
        List<ExpiredOrderInfo> result = new ArrayList<>();
        String sql = "SELECT o.orderId, o.customerId, o.placedAt, o.totalAmount, "
                + "c.email, c.fullName "
                + "FROM Orders o "
                + "JOIN Customers c ON c.customerId = o.customerId "
                + "WHERE o.orderStatus = ? "
                + "AND o.placedAt <= DATEADD(DAY, -2, GETDATE()) "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM Payments p "
                + "  WHERE p.orderId = o.orderId AND p.paymentType = ?"
                + ") ORDER BY o.placedAt ASC";

        try (Connection conn = new DBContext().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, OrderStatus.PENDING);
            ps.setString(2, PaymentType.PURCHASE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp placedAtValue = rs.getTimestamp("placedAt");
                    result.add(new ExpiredOrderInfo(
                            rs.getString("orderId"),
                            rs.getString("customerId"),
                            rs.getString("email"),
                            rs.getString("fullName"),
                            placedAtValue == null ? null : placedAtValue.toLocalDateTime(),
                            rs.getBigDecimal("totalAmount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("getExpiredPendingOrders error: " + e.getMessage());
        }
        return result;
    }

    public boolean expirePendingOrder(String orderId) {
        String lockSql = "SELECT orderStatus, placedAt FROM Orders "
                + "WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND NOT EXISTS ("
                + "  SELECT 1 FROM Payments p WITH (UPDLOCK, HOLDLOCK) "
                + "  WHERE p.orderId = Orders.orderId AND p.paymentType = ?"
                + ")";
        String deleteOrderSql = "DELETE FROM Orders "
                + "WHERE orderId = ? AND orderStatus = ? "
                + "AND placedAt <= DATEADD(DAY, -2, GETDATE()) "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM Payments p "
                + "  WHERE p.orderId = Orders.orderId AND p.paymentType = ?"
                + ")";

        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);
            try {
                String orderStatus = null;
                LocalDateTime placedAt = null;

                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setString(1, orderId);
                    ps.setString(2, PaymentType.PURCHASE);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            orderStatus = rs.getString("orderStatus");
                            Timestamp value = rs.getTimestamp("placedAt");
                            placedAt = value == null ? null : value.toLocalDateTime();
                        }
                    }
                }

                if (!OrderStatus.PENDING.equals(orderStatus)
                        || placedAt == null
                        || placedAt.plusDays(2).isAfter(LocalDateTime.now())) {
                    conn.rollback();
                    return false;
                }

                if (!releaseReservedStock(conn, orderId)) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteOrderSql)) {
                    ps.setString(1, orderId);
                    ps.setString(2, OrderStatus.PENDING);
                    ps.setString(3, PaymentType.PURCHASE);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("expirePendingOrder error for " + orderId
                        + ": " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("expirePendingOrder connection error: " + e.getMessage());
            return false;
        }
    }

    private boolean releaseReservedStock(Connection conn, String orderId)
            throws SQLException {
        Map<String, Integer> quantities = new TreeMap<>();
        String selectSql = "SELECT variantId, SUM(quantity) AS quantity "
                + "FROM OrderItems WHERE orderId = ? GROUP BY variantId ORDER BY variantId";
        String updateSql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET reservedQty = reservedQty - ? "
                + "WHERE variantId = ? AND reservedQty >= ?";

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quantities.put(rs.getString("variantId"), rs.getInt("quantity"));
                }
            }
        }

        if (quantities.isEmpty()) {
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
                int quantity = entry.getValue();
                ps.setInt(1, quantity);
                ps.setString(2, entry.getKey());
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }
        }
        return true;
    }
}
