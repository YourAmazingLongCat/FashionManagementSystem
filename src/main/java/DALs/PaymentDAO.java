package DALs;

import Models.Payment;
import Utils.DBContext;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import Utils.PaymentType;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for order payments.
 *
 * <p>Payment attempts are stored in the generic {@code Payments} table. The
 * current payment summary is mirrored onto {@code Orders} so the Order, Bill
 * and Payment responsibilities remain integrated in the Order module.</p>
 */
public class PaymentDAO extends DBContext {

    public PaymentDAO() {
        super();
    }

    public boolean createPayment(Payment payment) {
        if (!isValidPayment(payment)) {
            return false;
        }

        String sql = "INSERT INTO Payments "
                + "(paymentId, orderId, paymentType, paymentMethod, paymentStatus, "
                + "amount, description, createdAt, completedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setPaymentParameters(ps, payment);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("createPayment error: " + e.getMessage());
            return false;
        }
    }

    public Payment getPaymentById(String paymentId) {
        if (isBlank(paymentId)) {
            return null;
        }

        String sql = "SELECT * FROM Payments WHERE paymentId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getPaymentFromResultSet(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("getPaymentById error: " + e.getMessage());
            return null;
        }
    }

    public Payment getPaymentByIdAndAccountId(String paymentId, String customerId) {
        if (isBlank(paymentId) || isBlank(customerId)) {
            return null;
        }

        String sql = "SELECT p.* FROM Payments p "
                + "JOIN Orders o ON o.orderId = p.orderId "
                + "WHERE p.paymentId = ? AND o.customerId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentId.trim());
            ps.setString(2, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getPaymentFromResultSet(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("getPaymentByIdAndAccountId error: " + e.getMessage());
            return null;
        }
    }

    public Payment getLatestPaymentByOrderId(String orderId) {
        if (isBlank(orderId)) {
            return null;
        }

        String sql = "SELECT TOP 1 * FROM Payments "
                + "WHERE orderId = ? AND paymentType = ? "
                + "ORDER BY createdAt DESC, paymentId DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId.trim());
            ps.setString(2, PaymentType.PURCHASE);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getPaymentFromResultSet(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("getLatestPaymentByOrderId error: " + e.getMessage());
            return null;
        }
    }

    public List<Payment> getPaymentsByAccountId(String customerId) {
        List<Payment> payments = new ArrayList<>();
        if (isBlank(customerId)) {
            return payments;
        }

        String sql = "SELECT p.* FROM Payments p "
                + "JOIN Orders o ON o.orderId = p.orderId "
                + "WHERE o.customerId = ? "
                + "ORDER BY p.createdAt DESC, p.paymentId DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(getPaymentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getPaymentsByAccountId error: " + e.getMessage());
        }
        return payments;
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM Payments ORDER BY createdAt DESC, paymentId DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(getPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllPayments error: " + e.getMessage());
        }
        return payments;
    }

    public int countAllPayments() {
        String sql = "SELECT COUNT(*) FROM Payments";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.out.println("countAllPayments error: " + e.getMessage());
            return 0;
        }
    }

    public List<Payment> getAllPaymentsPaginated(int offset, int limit) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM Payments "
                + "ORDER BY createdAt DESC, paymentId DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, offset));
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(getPaymentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getAllPaymentsPaginated error: " + e.getMessage());
        }
        return payments;
    }

    /**
     * Creates a Pending purchase payment and updates the order summary in the
     * same database transaction.
     */
    public boolean createOrderPayment(Payment payment, String customerId) {
        if (!isValidPayment(payment) || isBlank(customerId)
                || !PaymentType.PURCHASE.equals(payment.getPaymentType())
                || !PaymentStatus.PENDING.equals(payment.getPaymentStatus())
                || !PaymentMethod.isSupported(payment.getPaymentMethod())) {
            return false;
        }

        String lockOrderSql = "SELECT totalAmount, orderStatus FROM Orders "
                + "WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND customerId = ?";
        String activePaymentSql = "SELECT TOP 1 paymentId FROM Payments "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE orderId = ? AND paymentType = ? "
                + "AND paymentStatus IN (?, ?)";
        String insertPaymentSql = "INSERT INTO Payments "
                + "(paymentId, orderId, paymentType, paymentMethod, paymentStatus, "
                + "amount, description, createdAt, completedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateOrderSql = "UPDATE Orders SET paymentMethod = ?, "
                + "paymentStatus = ?, paidAmount = 0, "
                + "issuedDate = COALESCE(issuedDate, GETDATE()) "
                + "WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);

            BigDecimal orderAmount = null;
            String orderStatus = null;
            try (PreparedStatement ps = connection.prepareStatement(lockOrderSql)) {
                ps.setString(1, payment.getOrderId().trim());
                ps.setString(2, customerId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderAmount = rs.getBigDecimal("totalAmount");
                        orderStatus = rs.getString("orderStatus");
                    }
                }
            }

            if (orderAmount == null || orderAmount.compareTo(payment.getAmount()) != 0
                    || "Cancelled".equalsIgnoreCase(orderStatus)
                    || "Delivered".equalsIgnoreCase(orderStatus)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(activePaymentSql)) {
                ps.setString(1, payment.getOrderId().trim());
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, PaymentStatus.PENDING);
                ps.setString(4, PaymentStatus.PAID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        connection.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(insertPaymentSql)) {
                setPaymentParameters(ps, payment);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                ps.setString(1, payment.getPaymentMethod());
                ps.setString(2, PaymentStatus.PENDING);
                ps.setString(3, payment.getOrderId().trim());
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("createOrderPayment", e);
            return false;
        } finally {
            restoreAutoCommit("createOrderPayment");
        }
    }

    public boolean completeCashPayment(String orderId) {
        return updatePendingPayment(
                orderId,
                PaymentMethod.COD,
                PaymentStatus.PAID,
                "COD payment collected on delivery."
        );
    }

    public boolean cancelCashPayment(String orderId) {
        return updatePendingPayment(
                orderId,
                PaymentMethod.COD,
                PaymentStatus.CANCELLED,
                "COD payment cancelled with the order."
        );
    }

    public boolean completeVNPayPurchase(String paymentId, BigDecimal amount,
            String description) {
        if (isBlank(paymentId) || amount == null) {
            return false;
        }

        String lockSql = "SELECT orderId FROM Payments WITH (UPDLOCK, ROWLOCK) "
                + "WHERE paymentId = ? AND paymentType = ? AND paymentMethod = ? "
                + "AND paymentStatus = ? AND amount = ?";
        String updatePaymentSql = "UPDATE Payments SET paymentStatus = ?, "
                + "completedAt = GETDATE(), description = COALESCE(NULLIF(?, ''), description) "
                + "WHERE paymentId = ? AND paymentStatus = ?";
        String updateOrderSql = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, "
                + "paidAmount = ?, issuedDate = COALESCE(issuedDate, GETDATE()) "
                + "WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);
            String orderId = null;
            try (PreparedStatement ps = connection.prepareStatement(lockSql)) {
                ps.setString(1, paymentId.trim());
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, PaymentMethod.VNPAY);
                ps.setString(4, PaymentStatus.PENDING);
                ps.setBigDecimal(5, amount);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getString("orderId");
                    }
                }
            }

            if (orderId == null) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(updatePaymentSql)) {
                ps.setString(1, PaymentStatus.PAID);
                ps.setString(2, description);
                ps.setString(3, paymentId.trim());
                ps.setString(4, PaymentStatus.PENDING);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                ps.setString(1, PaymentMethod.VNPAY);
                ps.setString(2, PaymentStatus.PAID);
                ps.setBigDecimal(3, amount);
                ps.setString(4, orderId);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("completeVNPayPurchase", e);
            return false;
        } finally {
            restoreAutoCommit("completeVNPayPurchase");
        }
    }

    public boolean markVNPayPaymentUnsuccessful(String paymentId, BigDecimal amount,
            String newStatus, String description) {
        if (isBlank(paymentId) || amount == null
                || (!PaymentStatus.FAILED.equals(newStatus)
                && !PaymentStatus.CANCELLED.equals(newStatus))) {
            return false;
        }

        String lockSql = "SELECT orderId FROM Payments WITH (UPDLOCK, ROWLOCK) "
                + "WHERE paymentId = ? AND paymentMethod = ? "
                + "AND paymentStatus = ? AND amount = ?";
        String updatePaymentSql = "UPDATE Payments SET paymentStatus = ?, "
                + "completedAt = GETDATE(), description = COALESCE(NULLIF(?, ''), description) "
                + "WHERE paymentId = ? AND paymentStatus = ?";
        String updateOrderSql = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, "
                + "paidAmount = 0 WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);
            String orderId = null;
            try (PreparedStatement ps = connection.prepareStatement(lockSql)) {
                ps.setString(1, paymentId.trim());
                ps.setString(2, PaymentMethod.VNPAY);
                ps.setString(3, PaymentStatus.PENDING);
                ps.setBigDecimal(4, amount);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getString("orderId");
                    }
                }
            }

            if (orderId == null) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(updatePaymentSql)) {
                ps.setString(1, newStatus);
                ps.setString(2, description);
                ps.setString(3, paymentId.trim());
                ps.setString(4, PaymentStatus.PENDING);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                ps.setString(1, PaymentMethod.VNPAY);
                ps.setString(2, newStatus);
                ps.setString(3, orderId);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("markVNPayPaymentUnsuccessful", e);
            return false;
        } finally {
            restoreAutoCommit("markVNPayPaymentUnsuccessful");
        }
    }

    public boolean refundVNPayPayment(String paymentId, String refundDescription) {
        Payment payment = getPaymentById(paymentId);
        if (payment == null || !PaymentMethod.VNPAY.equals(payment.getPaymentMethod())
                || !PaymentStatus.isPaid(payment.getPaymentStatus())) {
            return false;
        }
        return refundVNPayPaymentByOrderId(payment.getOrderId(), refundDescription);
    }

    public boolean refundVNPayPaymentByOrderId(String orderId, String refundDescription) {
        if (isBlank(orderId)) {
            return false;
        }

        String selectSql = "SELECT TOP 1 paymentId FROM Payments "
                + "WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND paymentType = ? AND paymentMethod = ? "
                + "AND paymentStatus = ? ORDER BY createdAt DESC, paymentId DESC";
        String updatePaymentSql = "UPDATE Payments SET paymentStatus = ?, "
                + "completedAt = GETDATE(), description = COALESCE(NULLIF(?, ''), description) "
                + "WHERE paymentId = ? AND paymentStatus = ?";
        String updateOrderSql = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, "
                + "paidAmount = 0 WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);
            String paymentId = null;
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                ps.setString(1, orderId.trim());
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, PaymentMethod.VNPAY);
                ps.setString(4, PaymentStatus.PAID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paymentId = rs.getString("paymentId");
                    }
                }
            }

            if (paymentId == null) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(updatePaymentSql)) {
                ps.setString(1, PaymentStatus.REFUNDED);
                ps.setString(2, refundDescription);
                ps.setString(3, paymentId);
                ps.setString(4, PaymentStatus.PAID);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                ps.setString(1, PaymentMethod.VNPAY);
                ps.setString(2, PaymentStatus.REFUNDED);
                ps.setString(3, orderId.trim());
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("refundVNPayPaymentByOrderId", e);
            return false;
        } finally {
            restoreAutoCommit("refundVNPayPaymentByOrderId");
        }
    }

    private boolean updatePendingPayment(String orderId, String method,
            String newStatus, String description) {
        if (isBlank(orderId) || !PaymentMethod.isSupported(method)) {
            return false;
        }

        String selectSql = "SELECT TOP 1 paymentId, amount FROM Payments "
                + "WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND paymentType = ? AND paymentMethod = ? "
                + "AND paymentStatus = ? ORDER BY createdAt DESC, paymentId DESC";
        String updatePaymentSql = "UPDATE Payments SET paymentStatus = ?, "
                + "completedAt = GETDATE(), description = COALESCE(NULLIF(?, ''), description) "
                + "WHERE paymentId = ? AND paymentStatus = ?";
        String updateOrderSql = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, "
                + "paidAmount = ? WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);
            String paymentId = null;
            BigDecimal amount = null;
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                ps.setString(1, orderId.trim());
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, method);
                ps.setString(4, PaymentStatus.PENDING);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paymentId = rs.getString("paymentId");
                        amount = rs.getBigDecimal("amount");
                    }
                }
            }

            if (paymentId == null || amount == null) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(updatePaymentSql)) {
                ps.setString(1, newStatus);
                ps.setString(2, description);
                ps.setString(3, paymentId);
                ps.setString(4, PaymentStatus.PENDING);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            BigDecimal paidAmount = PaymentStatus.PAID.equals(newStatus)
                    ? amount : BigDecimal.ZERO;
            try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                ps.setString(1, method);
                ps.setString(2, newStatus);
                ps.setBigDecimal(3, paidAmount);
                ps.setString(4, orderId.trim());
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("updatePendingPayment", e);
            return false;
        } finally {
            restoreAutoCommit("updatePendingPayment");
        }
    }

    private boolean isValidPayment(Payment payment) {
        return payment != null
                && !isBlank(payment.getPaymentId())
                && !isBlank(payment.getOrderId())
                && PaymentType.PURCHASE.equals(payment.getPaymentType())
                && PaymentMethod.isSupported(payment.getPaymentMethod())
                && !isBlank(payment.getPaymentStatus())
                && payment.getAmount() != null
                && payment.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private void setPaymentParameters(PreparedStatement ps, Payment payment)
            throws SQLException {
        ps.setString(1, payment.getPaymentId().trim());
        ps.setString(2, payment.getOrderId().trim());
        ps.setString(3, payment.getPaymentType());
        ps.setString(4, payment.getPaymentMethod());
        ps.setString(5, payment.getPaymentStatus());
        ps.setBigDecimal(6, payment.getAmount());
        ps.setString(7, payment.getDescription());
        ps.setTimestamp(8, Timestamp.valueOf(
                payment.getCreatedAt() == null ? LocalDateTime.now() : payment.getCreatedAt()));
        if (payment.getPaidAt() == null) {
            ps.setNull(9, java.sql.Types.TIMESTAMP);
        } else {
            ps.setTimestamp(9, Timestamp.valueOf(payment.getPaidAt()));
        }
    }

    private Payment getPaymentFromResultSet(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getString("paymentId"),
                rs.getString("orderId"),
                rs.getString("paymentType"),
                rs.getString("paymentMethod"),
                rs.getString("paymentStatus"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                toLocalDateTime(rs.getTimestamp("createdAt")),
                toLocalDateTime(rs.getTimestamp("completedAt"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void rollback(String action, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            System.out.println(action + " rollback error: " + rollbackError.getMessage());
        }
        System.out.println(action + " error: " + original.getMessage());
    }

    private void restoreAutoCommit(String action) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(action + " setAutoCommit error: " + e.getMessage());
        }
    }
}
