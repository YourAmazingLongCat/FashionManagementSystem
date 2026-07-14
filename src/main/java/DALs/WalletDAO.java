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

    public Wallet getWalletByAccountId(String accountId) {
        String query = "SELECT * FROM Wallets WHERE accountId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getWalletFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getWalletByAccountId error: " + e);
        }

        return null;
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
        String query = "INSERT INTO Wallets "
                + "(walletId, accountId, balance, walletStatus, createdAt, updatedAt) "
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
}
