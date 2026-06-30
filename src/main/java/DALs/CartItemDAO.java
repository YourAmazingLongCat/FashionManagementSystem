package DALs;

import Models.CartItemView;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CartItemDAO extends DBContext {

    public boolean existsItem(String cartId, String variantId) {

        String sql =
                "SELECT * FROM CartItems "
                + "WHERE cartId=? AND variantId=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, cartId);
            ps.setString(2, variantId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void addItem(String cartId, String variantId, int quantity) {

        String itemId = "CI" + System.currentTimeMillis();

        String sql =
                "INSERT INTO CartItems "
                + "(cartItemId, cartId, variantId, quantity) "
                + "VALUES(?,?,?,?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, itemId);
            ps.setString(2, cartId);
            ps.setString(3, variantId);
            ps.setInt(4, quantity);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void increaseQuantity(String cartId, String variantId, int quantity) {

        String sql =
                "UPDATE CartItems "
                + "SET quantity = quantity + ? "
                + "WHERE cartId=? AND variantId=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, quantity);
            ps.setString(2, cartId);
            ps.setString(3, variantId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // GET CART ITEMS (FIXED)
    // =========================
    public List<CartItemView> getCartItems(String cartId) {

        List<CartItemView> list = new ArrayList<>();

        String sql =
                "SELECT "
                + "ci.cartItemId, "
                + "ci.variantId, "
                + "ci.quantity, "
                + "p.name AS productName, "
                + "s.sizeName, "
                + "c.colorName, "
                + "ISNULL(pv.priceOverride, p.basePrice) AS price, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS imageUrl "
                + "FROM CartItems ci "
                + "JOIN ProductVariants pv ON ci.variantId = pv.variantId "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE ci.cartId=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, cartId);

            ResultSet rs = ps.executeQuery();

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // UPDATE QUANTITY
    // =========================
    public void updateQuantity(String cartItemId, int quantity) {

        String sql =
                "UPDATE CartItems "
                + "SET quantity=? "
                + "WHERE cartItemId=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

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

        String sql =
                "DELETE FROM CartItems "
                + "WHERE cartItemId=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

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
        String sql = "DELETE FROM CartItems WHERE cartId=? AND variantId NOT IN (SELECT variantId FROM ProductVariants)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, cartId);
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

        String sql =
                "SELECT SUM(ISNULL(pv.priceOverride, p.basePrice) * ci.quantity) AS total "
                + "FROM CartItems ci "
                + "JOIN ProductVariants pv ON ci.variantId = pv.variantId "
                + "JOIN Products p ON pv.productId = p.productId "
                + "WHERE ci.cartId=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, cartId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}