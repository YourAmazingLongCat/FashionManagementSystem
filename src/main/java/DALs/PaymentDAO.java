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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO extends DBContext {

    public PaymentDAO() {
        super();
    }

    public boolean createPayment(Payment payment) {
        String query = "INSERT INTO Payments "
                + "(paymentId, walletId, orderId, paymentType, paymentMethod, paymentStatus, "
                + "amount, description, createdAt, paidAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            setPaymentParameters(ps, payment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("createPayment error: " + e);
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
        String query = "SELECT TOP 1 * FROM Payments WHERE orderId = ? ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);

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
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.out.println("completeDeposit rollback error: " + ex);
            }
            System.out.println("completeDeposit error: " + e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("completeDeposit setAutoCommit error: " + e);
            }
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
                        wallet = new Wallet(
                                rs.getString("walletId"),
                                rs.getString("accountId"),
                                rs.getBigDecimal("balance"),
                                rs.getString("walletStatus"),
                                toLocalDateTime(rs.getTimestamp("createdAt")),
                                toLocalDateTime(rs.getTimestamp("updatedAt"))
                        );
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
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.out.println("payOrderWithWallet rollback error: " + ex);
            }
            System.out.println("payOrderWithWallet error: " + e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("payOrderWithWallet setAutoCommit error: " + e);
            }
        }

        return false;
    }

    private void setPaymentParameters(PreparedStatement ps, Payment payment) throws SQLException {
        ps.setString(1, payment.getPaymentId());
        ps.setString(2, payment.getWalletId());
        ps.setString(3, payment.getOrderId());
        ps.setString(4, payment.getPaymentType());
        ps.setString(5, payment.getPaymentMethod());
        ps.setString(6, payment.getPaymentStatus());
        ps.setBigDecimal(7, payment.getAmount());
        ps.setString(8, payment.getDescription());
        ps.setTimestamp(9, Timestamp.valueOf(payment.getCreatedAt()));

        if (payment.getPaidAt() == null) {
            ps.setTimestamp(10, null);
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

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
