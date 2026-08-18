package DALs;

import Models.Payment;
import Models.Wallet;
import Utils.DBContext;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import Utils.PaymentType;
import Utils.WalletStatus;
import Utils.WalletTransactionStatus;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the legacy {@link Payment} view of payments.
 *
 * The schema no longer has a Payments table. Instead:
 *   - Order payment method / status live on Orders (paymentMethod, paymentStatus, paidAmount).
 *   - Wallet ledger entries live on WalletTransactions (transactionId, walletId, orderId,
 *     transactionType, amount, transactionStatus, externalMethod, description, createdAt, completedAt).
 *
 * This DAO preserves the legacy {@link Payment} shape so existing controllers/services
 * keep compiling. The {@code paymentMethod} for an Order-bound entry is read from
 * Orders, while the underlying money movement (amount, status, completedAt, ...) is
 * taken from WalletTransactions.
 */
public class PaymentDAO extends DBContext {

    public PaymentDAO() {
        super();
    }

    /**
     * Create a new wallet transaction (Deposit / Purchase / Refund).
     * Returns true if the row was inserted.
     */
    public boolean createPayment(Payment payment) {
        if (connection == null) {
            System.out.println("createPayment error: database connection is null");
            return false;
        }

        if (payment == null || payment.getPaymentId() == null || payment.getPaymentId().trim().isEmpty()) {
            System.out.println("createPayment error: invalid payment object");
            return false;
        }

        String query = "INSERT INTO WalletTransactions "
                + "(transactionId, walletId, orderId, transactionType, amount, "
                + "transactionStatus, externalMethod, description, createdAt, completedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            setWalletTransactionParameters(ps, payment);
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
        String query = "SELECT * FROM WalletTransactions WHERE transactionId = ?";

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

    public Payment getPaymentByIdAndAccountId(String paymentId, String accountId) {
        String query = "SELECT wt.* FROM WalletTransactions wt "
                + "LEFT JOIN Wallets w ON wt.walletId = w.walletId "
                + "LEFT JOIN Orders o ON wt.orderId = o.orderId "
                + "WHERE wt.transactionId = ? AND (w.customerId = ? OR o.customerId = ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, paymentId);
            ps.setString(2, accountId);
            ps.setString(3, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getPaymentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getPaymentByIdAndAccountId error: " + e);
        }

        return null;
    }

    public Payment getLatestPaymentByOrderId(String orderId) {
        String query = "SELECT TOP 1 * FROM WalletTransactions "
                + "WHERE orderId = ? AND transactionType <> ? "
                + "ORDER BY createdAt DESC, transactionId DESC";

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
        String query = "SELECT * FROM WalletTransactions WHERE walletId = ? ORDER BY createdAt DESC";

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
        String query = "SELECT DISTINCT wt.* FROM WalletTransactions wt "
                + "LEFT JOIN Wallets w ON wt.walletId = w.walletId "
                + "LEFT JOIN Orders o ON wt.orderId = o.customerId "
                + "WHERE w.customerId = ? OR o.customerId = ? "
                + "ORDER BY wt.createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, accountId);
            ps.setString(2, accountId);

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
        String query = "SELECT * FROM WalletTransactions ORDER BY createdAt DESC";

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

    public int countAllPayments() {
        String query = "SELECT COUNT(*) FROM WalletTransactions";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("countAllPayments error: " + e);
        }
        return 0;
    }

    public List<Payment> getAllPaymentsPaginated(int offset, int limit) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM WalletTransactions ORDER BY createdAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, offset);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(getPaymentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getAllPaymentsPaginated error: " + e);
        }

        return payments;
    }

    public List<Payment> getPendingDeposits() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM WalletTransactions "
                + "WHERE transactionType = ? AND transactionStatus = ? "
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

    /**
     * Creates a Pending order payment only when the order belongs to the account
     * and no other active Purchase payment exists. The order row and payment
     * range are locked in one transaction to avoid concurrent payment methods.
     *
     * Implementation: this method is now legacy. The Orders table itself stores
     * paymentMethod/paymentStatus, and a placeholder WalletTransactions row is
     * created for the audit trail. To avoid duplicate Purchase rows, we check
     * Orders.paymentStatus before inserting.
     */
    public boolean createOrderPayment(Payment payment, String accountId) {
        if (payment == null || accountId == null || accountId.trim().isEmpty()
                || payment.getOrderId() == null || payment.getOrderId().trim().isEmpty()
                || !PaymentType.PURCHASE.equals(payment.getPaymentType())
                || !PaymentStatus.PENDING.equals(payment.getPaymentStatus())
                || (!PaymentMethod.COD.equals(payment.getPaymentMethod())
                && !PaymentMethod.VNPAY.equals(payment.getPaymentMethod()))
                || payment.getAmount() == null) {
            return false;
        }

        String lockOrderQuery = "SELECT totalAmount, orderStatus, paymentStatus FROM Orders "
                + "WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND customerId = ?";
        String activePaymentQuery = "SELECT TOP 1 transactionId FROM WalletTransactions "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE orderId = ? AND transactionType = ? "
                + "AND transactionStatus IN (?, ?) "
                + "ORDER BY createdAt DESC, transactionId DESC";
        String insertQuery = "INSERT INTO WalletTransactions "
                + "(transactionId, walletId, orderId, transactionType, amount, "
                + "transactionStatus, externalMethod, description, createdAt, completedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateOrderQuery = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, issuedDate = GETDATE() "
                + "WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);

            BigDecimal orderAmount = null;
            String orderStatus = null;
            String orderPaymentStatus = null;
            try (PreparedStatement ps = connection.prepareStatement(lockOrderQuery)) {
                ps.setString(1, payment.getOrderId().trim());
                ps.setString(2, accountId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderAmount = rs.getBigDecimal("totalAmount");
                        orderStatus = rs.getString("orderStatus");
                        orderPaymentStatus = rs.getString("paymentStatus");
                    }
                }
            }

            if (orderAmount == null || orderAmount.compareTo(payment.getAmount()) != 0
                    || "Cancelled".equals(orderStatus) || "Delivered".equals(orderStatus)) {
                connection.rollback();
                return false;
            }

            // Block duplicates if Order is already in a finalised payment state
            // or a non-cancelled payment ledger row already exists.
            if (PaymentStatus.PAID.equals(orderPaymentStatus)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(activePaymentQuery)) {
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

            try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
                setWalletTransactionParameters(ps, payment);
                if (ps.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
            }

            // Mirror the payment method/status onto Orders so the Order is now
            // discoverable by OrderDAO without needing a WalletTransactions scan.
            try (PreparedStatement ps = connection.prepareStatement(updateOrderQuery)) {
                ps.setString(1, payment.getPaymentMethod());
                ps.setString(2, PaymentStatus.PENDING);
                ps.setString(3, payment.getOrderId().trim());
                ps.executeUpdate();
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

    public boolean completeDeposit(String paymentId) {
        return completeDeposit(paymentId, null);
    }

    public boolean completeDeposit(String paymentId, String description) {
        String paymentQuery = "SELECT * FROM WalletTransactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE transactionId = ? AND transactionType = ? AND transactionStatus = ?";
        String updatePaymentQuery;
        String updateWalletQuery = "UPDATE Wallets "
                + "SET balance = balance + ?, updatedAt = GETDATE() "
                + "WHERE walletId = ? AND walletStatus = ?";

        if (description != null) {
            updatePaymentQuery = "UPDATE WalletTransactions "
                    + "SET transactionStatus = ?, completedAt = GETDATE(), description = ? "
                    + "WHERE transactionId = ?";
        } else {
            updatePaymentQuery = "UPDATE WalletTransactions "
                    + "SET transactionStatus = ?, completedAt = GETDATE() "
                    + "WHERE transactionId = ?";
        }

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
                // transactionStatus column requires {"Pending","Completed","Failed","Cancelled"} —
                // PaymentStatus.PAID ("Paid") is for Orders, here we must use Completed.
                ps.setString(1, WalletTransactionStatus.COMPLETED);
                if (description != null) {
                    ps.setString(2, description);
                    ps.setString(3, paymentId);
                } else {
                    ps.setString(2, paymentId);
                }

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
        String orderQuery = "SELECT totalAmount, orderStatus FROM Orders "
                + "WITH (UPDLOCK, HOLDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND customerId = ?";
        String activePaymentQuery = "SELECT TOP 1 transactionStatus FROM WalletTransactions "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE orderId = ? AND transactionType = ? "
                + "AND transactionStatus IN (?, ?) "
                + "ORDER BY createdAt DESC, transactionId DESC";
        String walletQuery = "SELECT * FROM Wallets WITH (UPDLOCK, ROWLOCK) "
                + "WHERE customerId = ?";
        String updateWalletQuery = "UPDATE Wallets "
                + "SET balance = balance - ?, updatedAt = GETDATE() "
                + "WHERE walletId = ? AND balance >= ? AND walletStatus = ?";
        String insertPaymentQuery = "INSERT INTO WalletTransactions "
                + "(transactionId, walletId, orderId, transactionType, amount, "
                + "transactionStatus, externalMethod, description, createdAt, completedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, NULL, ?, GETDATE(), GETDATE())";
        String updateOrderQuery = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, "
                + "paidAmount = ?, issuedDate = COALESCE(issuedDate, GETDATE()) "
                + "WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);

            BigDecimal orderAmount = null;
            String orderStatus = null;
            try (PreparedStatement ps = connection.prepareStatement(orderQuery)) {
                ps.setString(1, orderId);
                ps.setString(2, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderAmount = rs.getBigDecimal("totalAmount");
                        orderStatus = rs.getString("orderStatus");
                    }
                }
            }

            if (orderAmount == null || orderAmount.compareTo(amount) != 0
                    || "Cancelled".equals(orderStatus) || "Delivered".equals(orderStatus)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(activePaymentQuery)) {
                ps.setString(1, orderId);
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
                ps.setBigDecimal(5, amount);
                // WalletTransactions only allows Completed / Pending / Cancelled / Failed.
                ps.setString(6, WalletTransactionStatus.COMPLETED);
                ps.setString(7, description);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOrderQuery)) {
                ps.setString(1, PaymentMethod.WALLET);
                ps.setString(2, PaymentStatus.PAID);
                ps.setBigDecimal(3, amount);
                ps.setString(4, orderId);
                ps.executeUpdate();
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
        String query = "UPDATE WalletTransactions "
                + "SET transactionStatus = ?, completedAt = GETDATE() "
                + "WHERE orderId = ? "
                + "AND transactionType = ? "
                + "AND transactionStatus = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            // WalletTransactions only allows Completed / Pending / Cancelled / Failed.
            ps.setString(1, WalletTransactionStatus.COMPLETED);
            ps.setString(2, orderId);
            ps.setString(3, PaymentType.PURCHASE);
            ps.setString(4, WalletTransactionStatus.PENDING);

            int rows = ps.executeUpdate();
            // Also mirror on Orders so the Order shows Paid.
            if (rows > 0) {
                markOrderPaymentStatus(orderId, PaymentStatus.PAID);
            }
            return rows == 1;
        } catch (SQLException e) {
            System.out.println("completeCashPayment error: " + e);
        }

        return false;
    }

    public boolean cancelCashPayment(String orderId) {
        String query = "UPDATE WalletTransactions "
                + "SET transactionStatus = ? "
                + "WHERE orderId = ? "
                + "AND transactionType = ? "
                + "AND transactionStatus = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, WalletTransactionStatus.CANCELLED);
            ps.setString(2, orderId);
            ps.setString(3, PaymentType.PURCHASE);
            ps.setString(4, WalletTransactionStatus.PENDING);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                markOrderPaymentStatus(orderId, PaymentStatus.CANCELLED);
            }
            return rows == 1;
        } catch (SQLException e) {
            System.out.println("cancelCashPayment error: " + e);
        }

        return false;
    }

    public boolean completeVNPayPurchase(String paymentId, BigDecimal amount,
            String description) {
        String query = "UPDATE WalletTransactions "
                + "SET transactionStatus = ?, completedAt = GETDATE(), "
                + "description = COALESCE(NULLIF(?, ''), description) "
                + "WHERE transactionId = ? AND transactionType = ? "
                + "AND externalMethod = ? AND transactionStatus = ? AND amount = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            // WalletTransactions allows only Completed / Pending / Cancelled / Failed.
            ps.setString(1, WalletTransactionStatus.COMPLETED);
            ps.setString(2, description);
            ps.setString(3, paymentId);
            ps.setString(4, PaymentType.PURCHASE);
            ps.setString(5, PaymentMethod.VNPAY);
            ps.setString(6, WalletTransactionStatus.PENDING);
            ps.setBigDecimal(7, amount);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Find orderId for this transaction so we can mirror to Orders.
                Payment p = getPaymentById(paymentId);
                if (p != null && p.getOrderId() != null) {
                    markOrderPaymentStatusAndMethod(p.getOrderId(), PaymentMethod.VNPAY, PaymentStatus.PAID, amount);
                }
            }
            return rows == 1;
        } catch (SQLException e) {
            System.out.println("completeVNPayPurchase error: " + e);
            return false;
        }
    }

    public boolean markVNPayPaymentUnsuccessful(String paymentId, BigDecimal amount,
            String newStatus, String description) {
        if (!PaymentStatus.FAILED.equals(newStatus)
                && !PaymentStatus.CANCELLED.equals(newStatus)) {
            return false;
        }

        String query = "UPDATE WalletTransactions "
                + "SET transactionStatus = ?, completedAt = NULL, "
                + "description = COALESCE(NULLIF(?, ''), description) "
                + "WHERE transactionId = ? AND externalMethod = ? "
                + "AND transactionStatus = ? AND amount = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            // Map PaymentStatus value to a value WalletTransactions accepts.
            ps.setString(1, WalletTransactionStatus.fromPaymentStatus(newStatus));
            ps.setString(2, description);
            ps.setString(3, paymentId);
            ps.setString(4, PaymentMethod.VNPAY);
            ps.setString(5, WalletTransactionStatus.PENDING);
            ps.setBigDecimal(6, amount);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                Payment p = getPaymentById(paymentId);
                if (p != null && p.getOrderId() != null) {
                    markOrderPaymentStatusAndMethod(p.getOrderId(), PaymentMethod.VNPAY, newStatus, amount);
                }
            }
            return rows == 1;
        } catch (SQLException e) {
            System.out.println("markVNPayPaymentUnsuccessful error: " + e);
            return false;
        }
    }

    public boolean refundWalletPaymentIfNeeded(String orderId, String refundPaymentId) {
        String selectQuery = "SELECT TOP 1 * FROM WalletTransactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND transactionType = ? AND transactionStatus = ? "
                + "ORDER BY createdAt DESC, transactionId DESC";
        String updateOriginalPaymentQuery = "UPDATE WalletTransactions SET transactionStatus = ? WHERE transactionId = ?";
        String updateWalletQuery = "UPDATE Wallets SET balance = balance + ?, updatedAt = GETDATE() WHERE walletId = ?";
        String insertRefundQuery = "INSERT INTO WalletTransactions "
                + "(transactionId, walletId, orderId, transactionType, amount, "
                + "transactionStatus, externalMethod, description, createdAt, completedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, NULL, ?, GETDATE(), GETDATE())";

        try {
            connection.setAutoCommit(false);

            Payment paidWalletPayment = null;
            try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
                ps.setString(1, orderId);
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, PaymentStatus.PAID);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paidWalletPayment = getPaymentFromResultSet(rs);
                    }
                }
            }

            if (paidWalletPayment == null) {
                System.out.println("refundWalletPaymentIfNeeded: No PAID wallet payment found for orderId: " + orderId);
                connection.rollback();
                return true;
            }

            if (paidWalletPayment.getWalletId() == null) {
                System.out.println("refundWalletPaymentIfNeeded: WalletId is NULL for payment: " + paidWalletPayment.getPaymentId());
                connection.rollback();
                return false;
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
                // WalletTransactions does not allow 'Refunded' (CHECK constraint);
                // we leave the row in a final negative state via 'Cancelled'.
                ps.setString(1, WalletTransactionStatus.CANCELLED);
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
                ps.setBigDecimal(5, paidWalletPayment.getAmount());
                // Refund insert: WalletTransactions accepts 'Completed' for settled rows.
                ps.setString(6, WalletTransactionStatus.COMPLETED);
                ps.setString(7, "Refund wallet payment for cancelled order " + orderId);

                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            markOrderPaymentStatusAndMethod(orderId, PaymentMethod.WALLET, PaymentStatus.REFUNDED, paidWalletPayment.getAmount());

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("refundWalletPaymentIfNeeded", e);
        } finally {
            restoreAutoCommit("refundWalletPaymentIfNeeded");
        }

        return false;
    }

    /**
     * Refunds a VNPay payment by updating the transaction status to REFUNDED.
     * Note: Actual VNPay refund requires calling VNPay's refund API.
     */
    public boolean refundVNPayPayment(String paymentId, String refundDescription) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return false;
        }

        String query = "UPDATE WalletTransactions SET transactionStatus = ? WHERE transactionId = ? AND externalMethod = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            // WalletTransactions does not allow 'Refunded' (CHECK constraint).
            // Use 'Cancelled' as the terminal negative status for the original row.
            ps.setString(1, WalletTransactionStatus.CANCELLED);
            ps.setString(2, paymentId.trim());
            ps.setString(3, PaymentMethod.VNPAY);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("refundVNPayPayment error: " + e.getMessage());
        }

        return false;
    }

    public boolean refundVNPayPaymentByOrderId(String orderId, String refundPaymentId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }

        String selectQuery = "SELECT TOP 1 * FROM WalletTransactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE orderId = ? AND transactionType = ? AND externalMethod = ? AND transactionStatus = ? "
                + "ORDER BY createdAt DESC, transactionId DESC";
        String updateOriginalPaymentQuery = "UPDATE WalletTransactions SET transactionStatus = ? WHERE transactionId = ?";
        String insertRefundQuery = "INSERT INTO WalletTransactions "
                + "(transactionId, walletId, orderId, transactionType, amount, "
                + "transactionStatus, externalMethod, description, createdAt, completedAt) "
                + "VALUES (?, NULL, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

        try {
            connection.setAutoCommit(false);

            Payment paidVNPayPayment;
            try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
                ps.setString(1, orderId);
                ps.setString(2, PaymentType.PURCHASE);
                ps.setString(3, PaymentMethod.VNPAY);
                ps.setString(4, PaymentStatus.PAID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                    paidVNPayPayment = getPaymentFromResultSet(rs);
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOriginalPaymentQuery)) {
                // 'Refunded' is not allowed on WalletTransactions; use 'Cancelled'.
                ps.setString(1, WalletTransactionStatus.CANCELLED);
                ps.setString(2, paidVNPayPayment.getPaymentId());
                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            String refundDesc = (refundPaymentId == null || refundPaymentId.trim().isEmpty())
                    ? "Refund VNPay payment for cancelled order " + orderId
                    : "Manual refund for order " + orderId;

            try (PreparedStatement ps = connection.prepareStatement(insertRefundQuery)) {
                ps.setString(1, refundPaymentId);
                ps.setString(2, orderId);
                ps.setString(3, PaymentType.REFUND);
                ps.setBigDecimal(4, paidVNPayPayment.getAmount());
                // Refund insert must use a status allowed by the CHECK constraint.
                ps.setString(5, WalletTransactionStatus.COMPLETED);
                ps.setString(6, PaymentMethod.VNPAY);
                ps.setString(7, refundDesc);
                if (ps.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            markOrderPaymentStatusAndMethod(orderId, PaymentMethod.VNPAY, PaymentStatus.REFUNDED, paidVNPayPayment.getAmount());

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback("refundVNPayPaymentByOrderId", e);
        } finally {
            restoreAutoCommit("refundVNPayPaymentByOrderId");
        }

        return false;
    }

    private void markOrderPaymentStatus(String orderId, String paymentStatus) {
        String sql = "UPDATE Orders SET paymentStatus = ? WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("markOrderPaymentStatus error: " + e.getMessage());
        }
    }

    private void markOrderPaymentStatusAndMethod(String orderId, String paymentMethod,
            String paymentStatus, BigDecimal paidAmount) {
        String sql = "UPDATE Orders SET paymentMethod = ?, paymentStatus = ?, paidAmount = ? WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentMethod);
            ps.setString(2, paymentStatus);
            ps.setBigDecimal(3, paidAmount);
            ps.setString(4, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("markOrderPaymentStatusAndMethod error: " + e.getMessage());
        }
    }

    private void setWalletTransactionParameters(PreparedStatement ps, Payment payment) throws SQLException {
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

        // transactionType
        ps.setString(4, payment.getPaymentType());

        ps.setBigDecimal(5, payment.getAmount());

        // transactionStatus (must satisfy CK_WalletTransactions_Status)
        ps.setString(6, WalletTransactionStatus.fromPaymentStatus(payment.getPaymentStatus()));

        // externalMethod: only "VNPay" or NULL.
        if (PaymentMethod.VNPAY.equalsIgnoreCase(payment.getPaymentMethod())) {
            ps.setString(7, PaymentMethod.VNPAY);
        } else {
            ps.setNull(7, Types.VARCHAR);
        }

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

    /**
     * Maps a WalletTransactions row to the legacy Payment shape.
     *
     * The new schema stores payment method on Orders, not on the transaction
     * itself (the only stored method is {@code externalMethod = 'VNPay'}). We
     * therefore LEFT JOIN Orders to recover the canonical payment method for
     * each transaction when possible.
     */
    private Payment getPaymentFromResultSet(ResultSet rs) throws SQLException {
        String transactionType = rs.getString("transactionType");
        String transactionStatus = rs.getString("transactionStatus");
        String orderId = rs.getString("orderId");
        String externalMethod = rs.getString("externalMethod");

        String resolvedMethod;
        if ("VNPay".equalsIgnoreCase(externalMethod)) {
            resolvedMethod = PaymentMethod.VNPAY;
        } else if (transactionType != null && transactionType.equalsIgnoreCase(PaymentType.REFUND)) {
            // Refund rows: prefer Wallet if there is a walletId, otherwise VNPay.
            resolvedMethod = rs.getString("walletId") != null ? PaymentMethod.WALLET : PaymentMethod.VNPAY;
        } else if (orderId != null) {
            // Pull the canonical method from Orders if available.
            resolvedMethod = lookupOrderPaymentMethod(orderId);
        } else {
            // Pure deposit by VNPay is the only remaining case.
            resolvedMethod = PaymentMethod.VNPAY;
        }

        return new Payment(
                rs.getString("transactionId"),
                rs.getString("walletId"),
                orderId,
                transactionType,
                resolvedMethod,
                transactionStatus,
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                toLocalDateTime(rs.getTimestamp("createdAt")),
                toLocalDateTime(rs.getTimestamp("completedAt"))
        );
    }

    private String lookupOrderPaymentMethod(String orderId) {
        String sql = "SELECT paymentMethod FROM Orders WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String m = rs.getString("paymentMethod");
                    if (m != null) {
                        return m;
                    }
                }
            }
        } catch (SQLException ignore) {
            // ignore
        }
        return null;
    }

    private Wallet getWalletFromResultSet(ResultSet rs) throws SQLException {
        return new Wallet(
                rs.getString("walletId"),
                rs.getString("customerId"),
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