package DALs;

import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class WishlistDAO extends DBContext {

    public WishlistDAO() {
        super();
    }

    public Set<String> getWishlistProductIdsByAccountId(String accountId) {
        Set<String> wishlist = new LinkedHashSet<>();
        if (connection == null || accountId == null || accountId.isBlank()) {
            return wishlist;
        }

        // New schema: Wishlists.customerId replaces Wishlists.accountId.
        // Exclude products that have been hidden (status = 'Inactive') so
        // they no longer show up in the customer's wishlist even if they
        // were added before the status was changed.
        String sql = """
                SELECT w.productId
                FROM Wishlists w
                INNER JOIN Products p ON w.productId = p.productId
                WHERE w.customerId = ? AND ISNULL(p.status, '') <> 'Inactive'
                ORDER BY w.createdAt DESC
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    wishlist.add(rs.getString("productId"));
                }
            }
        } catch (SQLException ex) {
            System.out.println("getWishlistProductIdsByAccountId error: " + ex.getMessage());
        }
        return wishlist;
    }

    public boolean isInWishlist(String accountId, String productId) {
        if (connection == null || isBlank(accountId) || isBlank(productId)) {
            return false;
        }
        // A hidden (Inactive) product should not be reported as being in the
        // wishlist. The icon in the product list and the toggle API rely on
        // this, so we mirror the same status filter here.
        String sql = """
                SELECT 1
                FROM Wishlists w
                INNER JOIN Products p ON w.productId = p.productId
                WHERE w.customerId = ? AND w.productId = ?
                  AND ISNULL(p.status, '') <> 'Inactive'
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            System.out.println("isInWishlist error: " + ex.getMessage());
            return false;
        }
    }

    public boolean addToWishlist(String accountId, String productId) {
        if (connection == null || isBlank(accountId) || isBlank(productId)) {
            System.out.println("addToWishlist: invalid params - accountId=" + accountId + ", productId=" + productId);
            return false;
        }
        if (!isProductVisible(productId)) {
            System.out.println("addToWishlist: product is hidden (Inactive), refuse to add - productId=" + productId);
            return false;
        }
        if (isInWishlist(accountId, productId)) {
            return true;
        }
        String newId = generateNextWishlistId();
        String sql = "INSERT INTO Wishlists (wishlistId, customerId, productId, createdAt) VALUES (?, ?, ?, GETDATE())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, accountId);
            ps.setString(3, productId);
            int rows = ps.executeUpdate();
            System.out.println("addToWishlist: inserted " + rows + " row(s), id=" + newId);
            return rows > 0;
        } catch (SQLException ex) {
            System.out.println("addToWishlist error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean removeFromWishlist(String accountId, String productId) {
        if (connection == null || isBlank(accountId) || isBlank(productId)) {
            return false;
        }
        String sql = "DELETE FROM Wishlists WHERE customerId = ? AND productId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("removeFromWishlist error: " + ex.getMessage());
            return false;
        }
    }

    public boolean toggleWishlist(String accountId, String productId) {
        if (isInWishlist(accountId, productId)) {
            removeFromWishlist(accountId, productId);
            return false;
        } else {
            return addToWishlist(accountId, productId);
        }
    }

    /**
     * Returns true when the product exists and is not hidden
     * (status &lt;&gt; 'Inactive'). Hidden products must not be added to or
     * kept in the wishlist.
     */
    private boolean isProductVisible(String productId) {
        if (connection == null || isBlank(productId)) {
            return false;
        }
        String sql = "SELECT 1 FROM Products WHERE productId = ? AND ISNULL(status, '') <> 'Inactive'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            System.out.println("isProductVisible error: " + ex.getMessage());
            return false;
        }
    }

    private String generateNextWishlistId() {
        String sql = "SELECT TOP 1 wishlistId FROM Wishlists WHERE wishlistId LIKE 'WISH%' ORDER BY wishlistId DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String currentId = rs.getString("wishlistId");
                int number = Integer.parseInt(currentId.substring(4));
                return String.format("WISH%03d", number + 1);
            }
        } catch (Exception ex) {
            System.out.println("generateNextWishlistId error: " + ex.getMessage());
        }
        return "WISH001";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}