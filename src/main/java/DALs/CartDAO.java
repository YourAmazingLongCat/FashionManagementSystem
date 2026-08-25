/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DALs;

import Models.Cart;
import Models.CartItemView;
import Utils.DBContext;
import Utils.Utils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CartDAO extends DBContext {

    private static final String CUSTOMER_ID_PREFIX = "CART_";

    public CartDAO() {
        super();
    }


    public Cart getActiveCart(String accountId) {
        String sql = "SELECT customerId, COUNT(*) AS itemCount FROM Cart WHERE customerId = ? GROUP BY customerId";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("itemCount") > 0) {
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

    private static String customerIdFromCartId(String cartId) {
        if (cartId == null) return null;
        if (cartId.startsWith(CUSTOMER_ID_PREFIX)) {
            return cartId.substring(CUSTOMER_ID_PREFIX.length());
        }
        return cartId;
    }

    private String resolveCartId(String cartId) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || customerId.isBlank()) return cartId;
        String sql = "SELECT TOP 1 cartId FROM Cart WHERE customerId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("cartId");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cartId;
    }

    public boolean existsItem(String cartId, String variantId) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || variantId == null) return false;

        String sql = "SELECT 1 FROM Cart WHERE customerId = ? AND variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setString(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addItem(String cartId, String variantId, int quantity) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || variantId == null || quantity <= 0) return;

        String newCartItemId = Utils.generateId("CART");
        String sql = "INSERT INTO Cart (cartId, customerId, variantId, quantity) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newCartItemId);
            ps.setString(2, customerId);
            ps.setString(3, variantId);
            ps.setInt(4, quantity);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void increaseQuantity(String cartId, String variantId, int quantity) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || variantId == null || quantity <= 0) return;

        String sql = "UPDATE Cart SET quantity = quantity + ? WHERE customerId = ? AND variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, customerId);
            ps.setString(3, variantId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getQuantityInCart(String cartId, String variantId) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || variantId == null) return 0;

        String sql = "SELECT quantity FROM Cart WHERE customerId = ? AND variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setString(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getVariantIdByCartItemId(String cartItemId) {
        if (cartItemId == null || cartItemId.isBlank()) return null;

        String sql = "SELECT variantId FROM Cart WHERE cartId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cartItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("variantId");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CartItemView> getCartItems(String cartId) {
        List<CartItemView> list = new ArrayList<>();
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null) return list;

        String sql =
                "SELECT "
                + "ci.cartId AS cartItemId, "
                + "ci.variantId, "
                + "ci.quantity, "
                + "p.name AS productName, "
                + "s.sizeName, "
                + "c.colorName, "
                + "ISNULL(pv.priceOverride, p.basePrice) AS price, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS imageUrl "
                + "FROM Cart ci "
                + "JOIN ProductVariants pv ON ci.variantId = pv.variantId "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE ci.customerId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItemView item = new CartItemView();
                    item.setCartItemId(rs.getString("cartItemId"));
                    item.setVariantId(rs.getString("variantId"));
                    item.setProductName(rs.getString("productName"));
                    item.setSizeName(rs.getString("sizeName"));
                    item.setColorName(rs.getString("colorName"));
                    item.setImageUrl(rs.getString("imageUrl"));

                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");
                    item.setPrice(price);
                    item.setQuantity(quantity);
                    item.setSubtotal(price * quantity);
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

  
    public void updateQuantity(String cartItemId, int quantity) {
        if (cartItemId == null || cartItemId.isBlank()) return;

        String sql = "UPDATE Cart SET quantity = ? WHERE cartId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, cartItemId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(String cartItemId) {
        if (cartItemId == null || cartItemId.isBlank()) return;

        String sql = "DELETE FROM Cart WHERE cartId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cartItemId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int cleanupInvalidItems(String cartId) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null) return 0;

        String sql = "DELETE FROM Cart WHERE customerId = ? AND variantId NOT IN (SELECT variantId FROM ProductVariants)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getCartTotal(String cartId) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null) return 0;

        String sql =
                "SELECT SUM(ISNULL(pv.priceOverride, p.basePrice) * ci.quantity) AS total "
                + "FROM Cart ci "
                + "JOIN ProductVariants pv ON ci.variantId = pv.variantId "
                + "JOIN Products p ON pv.productId = p.productId "
                + "WHERE ci.customerId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<CartItemView> getCartItemsByIds(String cartId, String[] cartItemIds) {
        List<CartItemView> list = new ArrayList<>();
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || cartItemIds == null || cartItemIds.length == 0) return list;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < cartItemIds.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT "
                + "ci.cartId AS cartItemId, "
                + "ci.variantId, "
                + "ci.quantity, "
                + "p.name AS productName, "
                + "s.sizeName, "
                + "c.colorName, "
                + "ISNULL(pv.priceOverride, p.basePrice) AS price, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS imageUrl "
                + "FROM Cart ci "
                + "JOIN ProductVariants pv ON ci.variantId = pv.variantId "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE ci.customerId = ? AND ci.cartId IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            int paramIndex = 2;
            for (String cartItemId : cartItemIds) {
                ps.setString(paramIndex++, cartItemId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItemView item = new CartItemView();
                    item.setCartItemId(rs.getString("cartItemId"));
                    item.setVariantId(rs.getString("variantId"));
                    item.setProductName(rs.getString("productName"));
                    item.setSizeName(rs.getString("sizeName"));
                    item.setColorName(rs.getString("colorName"));
                    item.setImageUrl(rs.getString("imageUrl"));

                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");
                    item.setPrice(price);
                    item.setQuantity(quantity);
                    item.setSubtotal(price * quantity);

                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean deleteItemsByIds(String cartId, String[] cartItemIds) {
        String customerId = customerIdFromCartId(cartId);
        if (customerId == null || cartItemIds == null || cartItemIds.length == 0) return false;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < cartItemIds.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "DELETE FROM Cart WHERE customerId = ? AND cartId IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            int paramIndex = 2;
            for (String cartItemId : cartItemIds) {
                ps.setString(paramIndex++, cartItemId);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
