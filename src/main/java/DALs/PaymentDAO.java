package DALs;

import Models.Payment;
import Models.Wallet;
import Utils.DBContext;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import Utils.PaymentType;
import Utils.WalletStatus;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO extends DBContext {

    public PaymentDAO() {
        super();
    }

    public boolean createPayment(Payment payment) {
        if (connection == null) {
            System.out.println("createPayment error: database connection is null");
            return false;
        }

        if (payment == null || payment.getPaymentId() == null || payment.getPaymentId().trim().isEmpty()) {
            System.out.println("createPayment error: invalid payment object");
            return false;
        }

        String query = "INSERT INTO Payments "
                + "(paymentId, walletId, orderId, paymentType, paymentMethod, paymentStatus, "
                + "amount, description, createdAt, paidAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            setPaymentParameters(ps, payment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("createPayment error for orderId="
                    + payment.getOrderId()
                    + ", method=" + payment.getPaymentMethod()
                    + ", status=" + payment.getPaymentStatus()
                    + ": " + e.getMessage());
        }

        return false;
    }

    public Payment getPaymentById(String paymentId) {
        String query = "SELECT * FROM Payments WHERE paymentId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, paymentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getPaymentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getPaymentById error: " + e);
        }

        return null;
    }

    public Payment getLatestPaymentByOrderId(String orderId) {
        String query = "SELECT TOP 1 * FROM Payments "
                + "WHERE orderId = ? AND paymentType <> ? "
                + "ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);
            ps.setString(2, PaymentType.REFUND);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getPaymentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getLatestPaymentByOrderId error: " + e);
        }

        return null;
    }

    public List<Payment> getPaymentsByWalletId(String walletId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payments WHERE walletId = ? ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, walletId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(getPaymentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getPaymentsByWalletId error: " + e);
        }

        return payments;
    }

    public List<Payment> getPaymentsByAccountId(String accountId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT p.* FROM Payments p "
                + "INNER JOIN Wallets w ON p.walletId = w.walletId "
                + "WHERE w.accountId = ? "
                + "ORDER BY p.createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(getPaymentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getPaymentsByAccountId error: " + e);
        }

        return payments;
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payments ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(getPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllPayments error: " + e);
        }

        return payments;
    }

    public List<Payment> getPendingDeposits() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payments "
                + "WHERE paymentType = ? AND paymentStatus = ? "
                + "ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, PaymentType.DEPOSIT);
            ps.setString(2, PaymentStatus.PENDING);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(getPaymentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getPendingDeposits error: " + e);
        }

        return payments;
    }

    public boolean completeDeposit(String paymentId) {
        String paymentQuery = "SELECT * FROM Payments WITH (UPDLOCK, ROWLOCK) "
                + "WHERE paymentId = ? AND paymentType = ? AND paymentStatus = ?";
        String updatePaymentQuery = "UPDATE Payments "
                + "SET paymentStatus = ?, paidAt = GETDATE() "
                + "WHERE paymentId = ?";
        String updateWalletQuery = "UPDATE Wallets "
                + "SET balance = balance + ?, updatedAt = GETDATE() "
                + "WHERE walletId = ? AND walletStatus = ?";

        try {
            connection.setAutoCommit(false);

            Payment payment = null;
            try (PreparedStatement ps = connection.prepareStatement(paymentQuery)) {
                ps.setString(1, paymentId);
                ps.setString(2, PaymentType.DEPOSIT);
                ps.setString(3, PaymentStatus.PENDING);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        payment = getPaymentFromResultSet(rs);
                    }
                }
            }

            if (payment == null) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(updateWalletQuery)) {
                ps.setBigDecimal(1, payment.getAmount());
                ps.setString(2, payment.getWalletId());
                ps.setString(3, WalletStatus.ACTIVE);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updatePaymentQuery)) {
                ps.setString(1, PaymentStatus.PAID);
                ps.setString(2, paymentId);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("completeDeposit", e);
        } finally {
            restoreAutoCommit("completeDeposit");
        }

        return false;
    }

    public boolean payOrderWithWallet(String paymentId, String accountId, String orderId,
            BigDecimal amount, String description) {
        String walletQuery = "SELECT * FROM Wallets WITH (UPDLOCK, ROWLOCK) "
                + "WHERE accountId = ?";
        String updateWalletQuery = "UPDATE Wallets "
                + "SET balance = balance - ?, updatedAt = GETDATE() "
                + "WHERE walletId = ? AND balance >= ? AND walletStatus = ?";
        String insertPaymentQuery = "INSERT INTO Payments "
                + "(paymentId, walletId, orderId, paymentType, paymentMethod, paymentStatus, "
                + "amount, description, createdAt, paidAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

        try {
            connection.setAutoCommit(false);

            Wallet wallet = null;
            try (PreparedStatement ps = connection.prepareStatement(walletQuery)) {
                ps.setString(1, accountId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        wallet = getWalletFromResultSet(rs);
                    }
                }
            }

            if (wallet == null
                    || !WalletStatus.ACTIVE.equals(wallet.getWalletStatus())
                    || wallet.getBalance().compareTo(amount) < 0) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(updateWalletQuery)) {
                ps.setBigDecimal(1, amount);
                ps.setString(2, wallet.getWalletId());
                ps.setBigDecimal(3, amount);
                ps.setString(4, WalletStatus.ACTIVE);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(insertPaymentQuery)) {
                ps.setString(1, paymentId);
                ps.setString(2, wallet.getWalletId());
                ps.setString(3, orderId);
                ps.setString(4, PaymentType.PURCHASE);
                ps.setString(5, PaymentMethod.WALLET);
                ps.setString(6, PaymentStatus.PAID);
                ps.setBigDecimal(7, amount);
                ps.setString(8, description);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("payOrderWithWallet", e);
        } finally {
            restoreAutoCommit("payOrderWithWallet");
        }

        return false;
    }

    public boolean completeCashPayment(String orderId) {
        String query = "UPDATE Payments "
                + "SET paymentStatus = ?, paidAt = GETDATE() "
                + "WHERE orderId = ? "
                + "AND paymentType = ? "
                + "AND paymentStatus = ? "
                + "AND paymentMethod = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, PaymentStatus.PAID);
            ps.setString(2, orderId);
            ps.setString(3, PaymentType.PURCHASE);
            ps.setString(4, PaymentStatus.PENDING);
            ps.setString(5, PaymentMethod.COD);

            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.out.println("completeCashPayment error: " + e);
        }

        return false;
    }

    public boolean refundWalletPaymentIfNeeded(String orderId, String refundPaymentId) {
        String selectQuery = "SELECT TOP 1 * FROM Payments WITH (UPDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND paymentType = ? AND paymentMethod = ? AND paymentStatus = ? "
                + "ORDER BY createdAt DESC";
        String updateOriginalPaymentQuery = "UPDATE Payments SET paymentStatus = ? WHERE paymentId = ?";
        String updateWalletQuery = "UPDATE Wallets SET balance = balance + ?, updatedAt = GETDATE() WHERE walletId = ?";
        String insertRefundQuery = "INSERT INTO Payments "
                + "(paymentId, walletId, orderId, paymentType, paymentMethod, paymentStatus, "
                + "amount, description, createdAt, paidAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

        try {
            connection.setAutoCommit(false);

            Payment paidWalletPayment = null;
            try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
                ps.setString(1, orderId);
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, PaymentMethod.WALLET);
                ps.setString(4, PaymentStatus.PAID);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paidWalletPayment = getPaymentFromResultSet(rs);
                    }
                }
            }

            if (paidWalletPayment == null) {
                connection.rollback();
                return true;
            }

            try (PreparedStatement ps = connection.prepareStatement(updateWalletQuery)) {
                ps.setBigDecimal(1, paidWalletPayment.getAmount());
                ps.setString(2, paidWalletPayment.getWalletId());

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOriginalPaymentQuery)) {
                ps.setString(1, PaymentStatus.REFUNDED);
                ps.setString(2, paidWalletPayment.getPaymentId());

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(insertRefundQuery)) {
                ps.setString(1, refundPaymentId);
                ps.setString(2, paidWalletPayment.getWalletId());
                ps.setString(3, orderId);
                ps.setString(4, PaymentType.REFUND);
                ps.setString(5, PaymentMethod.WALLET);
                ps.setString(6, PaymentStatus.PAID);
                ps.setBigDecimal(7, paidWalletPayment.getAmount());
                ps.setString(8, "Refund wallet payment for cancelled order " + orderId);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("refundWalletPaymentIfNeeded", e);
        } finally {
            restoreAutoCommit("refundWalletPaymentIfNeeded");
        }

        return false;
    }

    private void setPaymentParameters(PreparedStatement ps, Payment payment) throws SQLException {
        ps.setString(1, payment.getPaymentId());

        if (payment.getWalletId() == null || payment.getWalletId().trim().isEmpty()) {
            ps.setNull(2, Types.VARCHAR);
        } else {
            ps.setString(2, payment.getWalletId());
        }

        if (payment.getOrderId() == null || payment.getOrderId().trim().isEmpty()) {
            ps.setNull(3, Types.VARCHAR);
        } else {
            ps.setString(3, payment.getOrderId());
        }

        ps.setString(4, payment.getPaymentType());
        ps.setString(5, payment.getPaymentMethod());
        ps.setString(6, payment.getPaymentStatus());
        ps.setBigDecimal(7, payment.getAmount());
        ps.setString(8, payment.getDescription());

        if (payment.getCreatedAt() == null) {
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        } else {
            ps.setTimestamp(9, Timestamp.valueOf(payment.getCreatedAt()));
        }

        if (payment.getPaidAt() == null) {
            ps.setNull(10, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(10, Timestamp.valueOf(payment.getPaidAt()));
        }
    }

    private Payment getPaymentFromResultSet(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getString("paymentId"),
                rs.getString("walletId"),
                rs.getString("orderId"),
                rs.getString("paymentType"),
                rs.getString("paymentMethod"),
                rs.getString("paymentStatus"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                toLocalDateTime(rs.getTimestamp("createdAt")),
                toLocalDateTime(rs.getTimestamp("paidAt"))
        );
    }

    private Wallet getWalletFromResultSet(ResultSet rs) throws SQLException {
        return new Wallet(
                rs.getString("walletId"),
                rs.getString("accountId"),
                rs.getBigDecimal("balance"),
                rs.getString("walletStatus"),
                toLocalDateTime(rs.getTimestamp("createdAt")),
                toLocalDateTime(rs.getTimestamp("updatedAt"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    private void rollback(String action, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            System.out.println(action + " rollback error: " + ex);
        }
        System.out.println(action + " error: " + original);
    }

    private void restoreAutoCommit(String action) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(action + " setAutoCommit error: " + e);
        }
    }
}
