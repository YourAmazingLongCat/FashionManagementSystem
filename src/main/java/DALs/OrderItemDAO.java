package DALs;

import Models.OrderItem;
import Models.CartItem;
import Utils.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO extends DBContext {

    public OrderItemDAO() {
        super();
    }

    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        List<OrderItem> listItems = new ArrayList<>();
        if (orderId == null || orderId.trim().isEmpty()) {
            return listItems;
        }

        String query = "SELECT oi.orderItemId, oi.orderId, oi.variantId, oi.quantity, "
                + "oi.unitPrice, oi.discountAmount, pv.sku, p.name AS productName, "
                + "s.sizeName, c.colorName, "
                + "COALESCE("
                + "  (SELECT TOP 1 piv.imageUrl FROM ProductImages piv "
                + "   WHERE piv.variantId = oi.variantId "
                + "   ORDER BY piv.isPrimary DESC, piv.imageId ASC), "
                + "  (SELECT TOP 1 pip.imageUrl FROM ProductImages pip "
                + "   WHERE pip.productId = p.productId "
                + "   ORDER BY pip.isPrimary DESC, pip.imageId ASC)"
                + ") AS imageUrl "
                + "FROM OrderItems oi "
                + "LEFT JOIN ProductVariants pv ON pv.variantId = oi.variantId "
                + "LEFT JOIN Products p ON p.productId = pv.productId "
                + "LEFT JOIN Sizes s ON s.sizeId = pv.sizeId "
                + "LEFT JOIN Colors c ON c.colorId = pv.colorId "
                + "WHERE oi.orderId = ? "
                + "ORDER BY oi.orderItemId";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getString("orderItemId"),
                            rs.getString("orderId"),
                            rs.getString("variantId"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unitPrice"),
                            rs.getBigDecimal("discountAmount")
                    );
                    item.setSku(rs.getString("sku"));
                    item.setProductName(rs.getString("productName"));
                    item.setSizeName(rs.getString("sizeName"));
                    item.setColorName(rs.getString("colorName"));
                    item.setImageUrl(rs.getString("imageUrl"));
                    listItems.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("getOrderItemsByOrderId error: " + e.getMessage());
        }

        return listItems;
    }

    /**
     * Rebuilds the checkout summary from persisted OrderItems. This lets a
     * customer leave the page and continue the same Pending order later,
     * without relying on the HTTP session cart.
     */
    public List<CartItem> getCheckoutItemsByOrderId(String orderId) {
        List<CartItem> items = new ArrayList<>();
        if (orderId == null || orderId.trim().isEmpty()) {
            return items;
        }

        String query = "SELECT oi.variantId, oi.quantity, oi.unitPrice, "
                + "pv.productId, pv.sizeId, pv.colorId, "
                + "p.name AS productName, s.sizeName, c.colorName, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi "
                + " WHERE pi.productId = p.productId "
                + " ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS imageUrl "
                + "FROM OrderItems oi "
                + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE oi.orderId = ? "
                + "ORDER BY oi.orderItemId";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setVariantId(rs.getString("variantId"));
                    item.setProductId(rs.getString("productId"));
                    item.setProductName(rs.getString("productName"));
                    item.setProductImageUrl(rs.getString("imageUrl"));
                    item.setSizeId(rs.getString("sizeId"));
                    item.setSizeName(rs.getString("sizeName"));
                    item.setColorId(rs.getString("colorId"));
                    item.setColorName(rs.getString("colorName"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unitPrice"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("getCheckoutItemsByOrderId error: " + e.getMessage());
        }

        return items;
    }

    public boolean addOrderItem(OrderItem item) {
        if (item == null) {
            return false;
        }

        String query = "INSERT INTO OrderItems "
                + "(orderItemId, orderId, variantId, quantity, unitPrice, discountAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, item.getOrderItemId());
            ps.setString(2, item.getOrderId());
            ps.setString(3, item.getVariantId());
            ps.setInt(4, item.getQuantity());
            ps.setBigDecimal(5, item.getUnitPrice());
            ps.setBigDecimal(6, item.getDiscountAmount() == null ? BigDecimal.ZERO : item.getDiscountAmount());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("addOrderItem error: " + e);
        }

        return false;
    }

    public boolean addOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        String query = "INSERT INTO OrderItems "
                + "(orderItemId, orderId, variantId, quantity, unitPrice, discountAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (OrderItem item : orderItems) {
                ps.setString(1, item.getOrderItemId());
                ps.setString(2, item.getOrderId());
                ps.setString(3, item.getVariantId());
                ps.setInt(4, item.getQuantity());
                ps.setBigDecimal(5, item.getUnitPrice());
                ps.setBigDecimal(6, item.getDiscountAmount() == null ? BigDecimal.ZERO : item.getDiscountAmount());
                ps.addBatch();
            }

            ps.executeBatch();
            return true;

        } catch (SQLException e) {
            System.out.println("addOrderItems error: " + e);
        }

        return false;
    }

    public boolean deleteOrderItemsByOrderId(String orderId) {
        String query = "DELETE FROM OrderItems WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);
            return ps.executeUpdate() >= 0;

        } catch (SQLException e) {
            System.out.println("deleteOrderItemsByOrderId error: " + e);
        }

        return false;
    }

    public boolean variantExistsInOrder(String orderId, String variantId) {
        String query = "SELECT 1 FROM OrderItems WHERE orderId = ? AND variantId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);
            ps.setString(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("variantExistsInOrder error: " + e);
        }

        return false;
    }
}
