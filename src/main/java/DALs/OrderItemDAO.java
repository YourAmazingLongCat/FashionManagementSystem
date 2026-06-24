package DALs;

import Models.OrderItem;
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
        String query = "SELECT * FROM OrderItems WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);

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

                    listItems.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("getOrderItemsByOrderId error: " + e);
        }

        return listItems;
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
}
