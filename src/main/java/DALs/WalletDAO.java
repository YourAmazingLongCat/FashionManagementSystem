package DALs;

import Models.Wallet;
import Utils.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class WalletDAO extends DBContext {

    public WalletDAO() {
        super();
    }

    public Wallet getWalletByCustomerId(String customerId) {
        String query = "SELECT * FROM Wallets WHERE customerId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getWalletFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getWalletByCustomerId error: " + e);
        }

        return null;
    }

    /**
     * Backward-compatible alias kept so older callers (and any service code that
     * hasn't been migrated yet) still compile. Treats the value as a customerId.
     */
    public Wallet getWalletByAccountId(String accountId) {
        return getWalletByCustomerId(accountId);
    }

    public Wallet getWalletById(String walletId) {
        String query = "SELECT * FROM Wallets WHERE walletId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, walletId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getWalletFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getWalletById error: " + e);
        }

        return null;
    }

    public boolean createWallet(Wallet wallet) {
        // New schema: Wallets(customerId) replaces Wallets(accountId).
        // Wallet.accountId maps to customerId.
        String query = "INSERT INTO Wallets "
                + "(walletId, customerId, balance, walletStatus, createdAt, updatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, wallet.getWalletId());
            ps.setString(2, wallet.getAccountId());
            ps.setBigDecimal(3, wallet.getBalance());
            ps.setString(4, wallet.getWalletStatus());
            ps.setTimestamp(5, Timestamp.valueOf(wallet.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(wallet.getUpdatedAt()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("createWallet error: " + e);
        }

        return false;
    }

    public boolean updateWalletStatus(String walletId, String walletStatus) {
        String query = "UPDATE Wallets SET walletStatus = ?, updatedAt = GETDATE() WHERE walletId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, walletStatus);
            ps.setString(2, walletId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateWalletStatus error: " + e);
        }

        return false;
    }

    private Wallet getWalletFromResultSet(ResultSet rs) throws SQLException {
        // The DB column is customerId; the Wallet POJO still calls it accountId.
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
}