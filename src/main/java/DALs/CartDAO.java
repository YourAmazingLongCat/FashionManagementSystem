/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DALs;

import Models.Cart;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for the Cart feature.
 *
 * The new schema collapses the old Carts + CartItems pair into a single Cart
 * table keyed on (cartId, customerId, variantId). The {@link Cart} POJO still
 * has the legacy (cartId, accountId, status) shape, so cartId is synthesised
 * as a per-customer constant and status is no longer queried.
 */
public class CartDAO extends DBContext {

    public Cart getActiveCart(String accountId) {
        String sql = "SELECT customerId, COUNT(*) AS itemCount FROM Cart WHERE customerId = ? GROUP BY customerId";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("itemCount") > 0) {
                // Synthesise a stable per-customer cartId so legacy callers
                // that compare it as a String keep working.
                return new Cart(
                        "CART_" + accountId,
                        accountId,
                        "Active"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createCart(String accountId) {
        // New schema has no separate Carts table; the cartId is derived from
        // the customerId so existing callers see a stable identifier.
        return "CART_" + accountId;
    }

    public boolean removeCartItem(String customerId, String variantId) {
        if (customerId == null || variantId == null) {
            return false;
        }
        String sql = "DELETE FROM Cart WHERE customerId = ? AND variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setString(2, variantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("removeCartItem error: " + ex.getMessage());
            return false;
        }
    }
}