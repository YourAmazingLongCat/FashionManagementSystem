package DALs;

import Models.CartItemView;
import Utils.DBContext;
import Utils.Utils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the Cart feature.
 *
 * <p>The new schema has a single flat {@code Cart} table — there is no
 * separate {@code CartItems} table. Every row is keyed by
 * {@code (cartId, customerId, variantId, quantity)}.</p>
 *
 * <p>Servlet callers still pass the legacy "cartId" string
 * ({@code "CART_<customerId>"}). For read/write APIs that operate on the
 * legacy "cartId" we extract the customerId and key on that, which is
 * the real foreign key in the schema.</p>
 */
public class CartItemDAO extends DBContext {

    private static final String CUSTOMER_ID_PREFIX = "CART_";

    /** Extract the customerId from the legacy "CART_<customerId>" cartId string. */
    private static String customerIdFromCartId(String cartId) {
        if (cartId == null) return null;
        if (cartId.startsWith(CUSTOMER_ID_PREFIX)) {
            return cartId.substring(CUSTOMER_ID_PREFIX.length());
        }
        return cartId;
    }

    /**
     * Get the Cart.cartId (PK) for the legacy cartId, i.e. the synthetic
     * "CART_<customerId>" wrapper. Returns the cartId of the first row for
     * the customer so that callers still get a non-null identifier.
     */
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

        // Use the standard Utils.generateId() so the new row's cartId (PK in
        // the new schema) matches the project's ID convention. Trying to
        // synthesise a longer id caused "string or binary data would be
        // truncated" on the cartId column.
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

    // =========================
    // GET CART ITEMS
    // =========================
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

    // =========================
    // UPDATE QUANTITY
    // =========================
    public void updateQuantity(String cartItemId, int quantity) {
        if (cartItemId == null || cartItemId.isBlank()) return;

        // New schema uses Cart.cartId as the row PK; same role as the legacy
        // "cartItemId" in the old CartItems table.
        String sql = "UPDATE Cart SET quantity = ? WHERE cartId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, cartItemId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // DELETE ITEM
    // =========================
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

    // =========================
    // CLEANUP INVALID ITEMS
    // =========================
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

    // =========================
    // CART TOTAL
    // =========================
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

    /**
     * Generates a unique cartId of the form "CART_<timestamp>" so we never
     * collide with the legacy legacy "CART_<customerId>" ids that callers
     * synthesise from the customerId.
     * @deprecated kept only for reference; use {@link Utils#generateId(String)}.
     */
    @Deprecated
    private synchronized String generateNextCartId() {
        return "CART_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}