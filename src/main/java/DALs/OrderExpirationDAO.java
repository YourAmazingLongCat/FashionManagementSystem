package DALs;

import Models.ExpiredOrderInfo;
import Utils.DBContext;
import Utils.OrderStatus;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import Utils.PaymentType;
import Utils.WalletTransactionStatus;
import java.math.BigDecimal;
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

public class OrderExpirationDAO {

    public List<ExpiredOrderInfo> getExpiredPendingOrders() {
        List<ExpiredOrderInfo> result = new ArrayList<>();

        // The new schema stores customer email/fullName on Customers, not Accounts.
        String sql = "SELECT o.orderId, o.customerId, o.placedAt, o.totalAmount, "
                + "c.email, c.fullName "
                + "FROM Orders o "
                + "JOIN Customers c ON c.customerId = o.customerId "
                + "WHERE o.orderStatus = ? "
                + "AND o.placedAt <= DATEADD(DAY, -2, GETDATE()) "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM WalletTransactions wt "
                + "  WHERE wt.orderId = o.orderId AND wt.transactionType = ?"
                + ") "
                + "ORDER BY o.placedAt ASC";

        try (Connection conn = new DBContext().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, OrderStatus.PENDING);
            ps.setString(2, PaymentType.PURCHASE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp placedAtValue = rs.getTimestamp("placedAt");
                    LocalDateTime placedAt = placedAtValue == null
                            ? null : placedAtValue.toLocalDateTime();

                    result.add(new ExpiredOrderInfo(
                            rs.getString("orderId"),
                            rs.getString("customerId"),
                            rs.getString("email"),
                            rs.getString("fullName"),
                            placedAt,
                            rs.getBigDecimal("totalAmount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("getExpiredPendingOrders error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Deletes an expired Pending order only when the customer has not placed it.
     * The order row, payment check and stock release are protected in one transaction.
     */
    public boolean expirePendingOrder(String orderId, String refundPaymentId) {
        String lockSql = "SELECT o.orderStatus, o.placedAt, "
                + "wt.transactionId AS paymentId, wt.walletId, wt.externalMethod AS paymentMethod, wt.transactionStatus AS paymentStatus, wt.amount "
                + "FROM Orders o WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "OUTER APPLY ("
                + "  SELECT TOP 1 transactionId, walletId, externalMethod, transactionStatus, amount "
                + "  FROM WalletTransactions WITH (UPDLOCK, HOLDLOCK) "
                + "  WHERE orderId = o.orderId AND transactionType = ? "
                + "  ORDER BY createdAt DESC"
                + ") wt "
                + "WHERE o.orderId = ?";

        String updateWalletSql = "UPDATE Wallets "
                + "SET balance = balance + ?, updatedAt = GETDATE() "
                + "WHERE walletId = ?";

        // No Bill entity in the new schema. Refund ledger row only.
        String insertRefundSql = "INSERT INTO WalletTransactions "
                + "(transactionId, walletId, orderId, transactionType, amount, "
                + "transactionStatus, externalMethod, description, createdAt, completedAt) "
                + "VALUES (?, ?, NULL, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

        String deletePaymentsSql = "DELETE FROM WalletTransactions WHERE orderId = ?";
        String deleteOrderSql = "DELETE FROM Orders "
                + "WHERE orderId = ? AND orderStatus = ? "
                + "AND placedAt <= DATEADD(DAY, -2, GETDATE()) "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM WalletTransactions wt "
                + "  WHERE wt.orderId = Orders.orderId AND wt.transactionType = ?"
                + ")";

        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);

            try {
                String paymentId = null;
                String walletId = null;
                String paymentMethod = null;
                String paymentStatus = null;
                BigDecimal amount = null;
                LocalDateTime placedAt = null;
                String orderStatus = null;

                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setString(1, PaymentType.PURCHASE);
                    ps.setString(2, orderId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        orderStatus = rs.getString("orderStatus");
                        Timestamp placedAtValue = rs.getTimestamp("placedAt");
                        placedAt = placedAtValue == null
                                ? null : placedAtValue.toLocalDateTime();
                        paymentId = rs.getString("paymentId");
                        walletId = rs.getString("walletId");
                        paymentMethod = rs.getString("paymentMethod");
                        paymentStatus = rs.getString("paymentStatus");
                        amount = rs.getBigDecimal("amount");
                    }
                }

                if (!OrderStatus.PENDING.equals(orderStatus)
                        || paymentId != null
                        || placedAt == null
                        || placedAt.plusDays(2).isAfter(LocalDateTime.now())) {
                    conn.rollback();
                    return false;
                }

                if (!releaseReservedStock(conn, orderId)) {
                    conn.rollback();
                    return false;
                }

                // WalletTransactions stores 'Completed' for paid rows (CHECK constraint forbids 'Paid').
                boolean isPaid = PaymentStatus.isPaid(paymentStatus);
                // externalMethod stores only "VNPay"; Wallet payments never reach
                // here because paymentId != null guards them out.
                boolean isVNPay = PaymentMethod.VNPAY.equals(paymentMethod);
                boolean isWallet = walletId != null && !isVNPay;

                if (paymentId != null && isPaid && isWallet) {
                    if (walletId == null || amount == null) {
                        conn.rollback();
                        return false;
                    }

                    try (PreparedStatement ps = conn.prepareStatement(updateWalletSql)) {
                        ps.setBigDecimal(1, amount);
                        ps.setString(2, walletId);
                        if (ps.executeUpdate() <= 0) {
                            conn.rollback();
                            return false;
                        }
                    }

                    // WalletTransactions CHECK constraint forbids 'Paid' / 'Refunded' —
                    // use 'Completed' to mark the audit refund row as finalised.
                    insertRefundAudit(conn, insertRefundSql, refundPaymentId,
                            walletId, PaymentMethod.WALLET, WalletTransactionStatus.COMPLETED,
                            amount, "Automatic Wallet refund for expired order " + orderId);
                } else if (paymentId != null && isPaid && isVNPay && amount != null) {
                    // Same constraint: 'Completed' is the closest legal terminal status.
                    insertRefundAudit(conn, insertRefundSql, refundPaymentId,
                            walletId, PaymentMethod.VNPAY, WalletTransactionStatus.COMPLETED,
                            amount, "Automatic VNPay refund record for expired order " + orderId);
                }

                try (PreparedStatement ps = conn.prepareStatement(deletePaymentsSql)) {
                    ps.setString(1, orderId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteOrderSql)) {
                    ps.setString(1, orderId);
                    ps.setString(2, OrderStatus.PENDING);
                    ps.setString(3, PaymentType.PURCHASE);
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("expirePendingOrder error for " + orderId + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("expirePendingOrder connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private boolean releaseReservedStock(Connection conn, String orderId) throws SQLException {
        Map<String, Integer> quantities = new TreeMap<>();
        String selectSql = "SELECT variantId, SUM(quantity) AS quantity "
                + "FROM OrderItems WHERE orderId = ? GROUP BY variantId ORDER BY variantId";
        // Allow the UPDATE to affect 0 rows when reservedQty is already 0 —
        // older Pending orders (or orders where stock was never reserved at
        // checkout) must not block the auto-expiry path. The presence of a
        // matching row in ProductVariants is still required.
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

    private void insertRefundAudit(Connection conn, String sql, String refundPaymentId,
            String walletId, String method, String status, BigDecimal amount,
            String description) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refundPaymentId);
            ps.setString(2, walletId);
            ps.setString(3, PaymentType.REFUND);
            ps.setBigDecimal(4, amount);
            ps.setString(5, status);
            // externalMethod: 'VNPay' or NULL. Wallet refunds stay NULL.
            if (PaymentMethod.VNPAY.equalsIgnoreCase(method)) {
                ps.setString(6, PaymentMethod.VNPAY);
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }
            ps.setString(7, description);

            if (ps.executeUpdate() <= 0) {
                throw new SQLException("Could not create refund audit record.");
            }
        }
    }
}