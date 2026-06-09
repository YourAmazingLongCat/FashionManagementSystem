package DALs;

import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class FavoriteProductDAO extends DBContext {

    public FavoriteProductDAO() {
        super();
        ensureTableExists();
    }

    public Set<String> getFavoriteProductIdsByAccountId(String accountId) {
        Set<String> favorites = new LinkedHashSet<>();
        if (connection == null || accountId == null || accountId.isBlank()) {
            return favorites;
        }

        String sql = "SELECT productId FROM FavoriteProducts WHERE accountId = ? ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favorites.add(rs.getString("productId"));
                }
            }
        } catch (SQLException ex) {
            System.out.println("getFavoriteProductIdsByAccountId error: " + ex.getMessage());
        }
        return favorites;
    }

    public boolean isFavorite(String accountId, String productId) {
        if (connection == null || isBlank(accountId) || isBlank(productId)) {
            return false;
        }
        String sql = "SELECT 1 FROM FavoriteProducts WHERE accountId = ? AND productId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            System.out.println("isFavorite error: " + ex.getMessage());
            return false;
        }
    }

    public boolean addFavorite(String accountId, String productId) {
        if (connection == null || isBlank(accountId) || isBlank(productId)) {
            return false;
        }
        if (isFavorite(accountId, productId)) {
            return true;
        }
        String sql = "INSERT INTO FavoriteProducts (favoriteId, accountId, productId, createdAt) VALUES (?, ?, ?, GETDATE())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, generateNextFavoriteId());
            ps.setString(2, accountId);
            ps.setString(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("addFavorite error: " + ex.getMessage());
            return false;
        }
    }

    public boolean removeFavorite(String accountId, String productId) {
        if (connection == null || isBlank(accountId) || isBlank(productId)) {
            return false;
        }
        String sql = "DELETE FROM FavoriteProducts WHERE accountId = ? AND productId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("removeFavorite error: " + ex.getMessage());
            return false;
        }
    }

    private void ensureTableExists() {
        if (connection == null) {
            return;
        }
        String sql = "IF OBJECT_ID('dbo.FavoriteProducts', 'U') IS NULL "
                + "BEGIN "
                + "CREATE TABLE dbo.FavoriteProducts ("
                + "favoriteId VARCHAR(20) NOT NULL PRIMARY KEY,"
                + "accountId VARCHAR(20) NOT NULL,"
                + "productId VARCHAR(20) NOT NULL,"
                + "createdAt DATETIME NULL DEFAULT GETDATE(),"
                + "CONSTRAINT UQ_FavoriteProducts UNIQUE (accountId, productId),"
                + "CONSTRAINT FK_FavoriteProducts_Accounts FOREIGN KEY (accountId) REFERENCES dbo.Accounts(accountId),"
                + "CONSTRAINT FK_FavoriteProducts_Products FOREIGN KEY (productId) REFERENCES dbo.Products(productId)"
                + ") END";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException ex) {
            System.out.println("ensureTableExists FavoriteProducts error: " + ex.getMessage());
        }
    }

    private String generateNextFavoriteId() {
        String sql = "SELECT TOP 1 favoriteId FROM FavoriteProducts WHERE favoriteId LIKE 'FAV%' ORDER BY favoriteId DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String currentId = rs.getString("favoriteId");
                int number = Integer.parseInt(currentId.substring(3));
                return String.format("FAV%03d", number + 1);
            }
        } catch (Exception ex) {
            System.out.println("generateNextFavoriteId error: " + ex.getMessage());
        }
        return "FAV001";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
