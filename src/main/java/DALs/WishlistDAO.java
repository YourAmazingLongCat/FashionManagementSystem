package DALs;

import Utils.DBContext;
import Utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Data Access Object for Favorite Product Management (Wishlist).
 * Supports:
 * - Viewing favorite products for a customer account
 * - Adding a product to the favorite wishlist
 * - Deleting a product from the favorite wishlist
 * - Checking if a product is currently in the wishlist
 * 
 * @author ngocpace191049-cmyk
 */
public class WishlistDAO extends DBContext {

    public WishlistDAO() {
        super();
    }

    /**
     * Retrieve the set of all product IDs in a customer's wishlist.
     */
    public Set<String> getWishlistProductIdsByAccountId(String accountId) {
        Set<String> wishlist = new LinkedHashSet<>();
        if (accountId == null || accountId.isBlank()) {
            return wishlist;
        }

        String sql = "SELECT w.productId "
                   + "FROM Wishlists w "
                   + "INNER JOIN Products p ON w.productId = p.productId "
                   + "WHERE w.customerId = ? AND ISNULL(p.status, '') <> 'Inactive' "
                   + "ORDER BY w.createdAt DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    wishlist.add(rs.getString("productId"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("getWishlistProductIdsByAccountId error: " + ex.getMessage());
        }
        return wishlist;
    }

    /**
     * Check if a specific product is in the customer's wishlist.
     */
    public boolean isInWishlist(String accountId, String productId) {
        if (isBlank(accountId) || isBlank(productId)) {
            return false;
        }

        String sql = "SELECT 1 FROM Wishlists w "
                   + "INNER JOIN Products p ON w.productId = p.productId "
                   + "WHERE w.customerId = ? AND w.productId = ? AND ISNULL(p.status, '') <> 'Inactive'";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            System.err.println("isInWishlist error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Add product to wishlist.
     */
    public boolean addToWishlist(String accountId, String productId) {
        if (isBlank(accountId) || isBlank(productId)) {
            return false;
        }
        if (!isProductVisible(productId)) {
            return false;
        }
        if (isInWishlist(accountId, productId)) {
            return true;
        }

        String newId = Utils.generateId("WISH");
        String sql = "INSERT INTO Wishlists (wishlistId, customerId, productId, createdAt) VALUES (?, ?, ?, GETDATE())";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, accountId);
            ps.setString(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("addToWishlist error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Delete product from wishlist.
     */
    public boolean removeFromWishlist(String accountId, String productId) {
        if (isBlank(accountId) || isBlank(productId)) {
            return false;
        }

        String sql = "DELETE FROM Wishlists WHERE customerId = ? AND productId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("removeFromWishlist error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Toggle wishlist state: adds if absent, removes if present.
     */
    public boolean toggleWishlist(String accountId, String productId) {
        if (isInWishlist(accountId, productId)) {
            removeFromWishlist(accountId, productId);
            return false;
        } else {
            addToWishlist(accountId, productId);
            return true;
        }
    }

    private boolean isProductVisible(String productId) {
        String sql = "SELECT status FROM Products WHERE productId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    return status != null && !"Inactive".equalsIgnoreCase(status);
                }
            }
        } catch (SQLException ex) {
            System.err.println("isProductVisible error: " + ex.getMessage());
        }
        return false;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}