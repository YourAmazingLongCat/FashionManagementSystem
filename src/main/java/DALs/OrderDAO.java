package DALs;

import Models.Order;
import Models.OrderItem;
import Utils.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO extends DBContext {

    public OrderDAO() {
        super();
    }

    public List<Order> getAllOrders() {
        List<Order> listOrders = new ArrayList<>();
        String query = "SELECT * FROM Orders ORDER BY placedAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listOrders.add(getOrderFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("getAllOrders error: " + e);
        }

        return listOrders;
    }

    public Order getOrderById(String orderId) {
        String query = "SELECT * FROM Orders WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getOrderFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("getOrderById error: " + e);
        }

        return null;
    }

    public Order getOrderByIdAndCustomerId(String orderId, String customerId) {
        String query = "SELECT * FROM Orders WHERE orderId = ? AND customerId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderId);
            ps.setString(2, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getOrderFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("getOrderByIdAndCustomerId error: " + e);
        }

        return null;
    }

    public List<Order> getOrdersByCustomerId(String customerId) {
        List<Order> listOrders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE customerId = ? ORDER BY placedAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listOrders.add(getOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("getOrdersByCustomerId error: " + e);
        }

        return listOrders;
    }

    public List<Order> searchOrdersByCustomerId(String customerId, String keyword) {
        List<Order> listOrders = new ArrayList<>();

        String query = "SELECT * FROM Orders "
                + "WHERE customerId = ? "
                + "AND (orderId LIKE ? OR orderStatus LIKE ? OR phone LIKE ? OR shippingAddress LIKE ?) "
                + "ORDER BY placedAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String searchValue = "%" + keyword + "%";

            ps.setString(1, customerId);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            ps.setString(5, searchValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listOrders.add(getOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("searchOrdersByCustomerId error: " + e);
        }

        return listOrders;
    }

    public List<Order> searchOrdersForStaff(String keyword) {
        List<Order> listOrders = new ArrayList<>();

        String query = "SELECT * FROM Orders "
                + "WHERE orderId LIKE ? "
                + "OR customerId LIKE ? "
                + "OR orderStatus LIKE ? "
                + "OR phone LIKE ? "
                + "OR shippingAddress LIKE ? "
                + "ORDER BY placedAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String searchValue = "%" + keyword + "%";

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            ps.setString(5, searchValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listOrders.add(getOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("searchOrdersForStaff error: " + e);
        }

        return listOrders;
    }

    public boolean createOrder(Order order, List<OrderItem> orderItems) {
        if (order == null || orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        String orderQuery = "INSERT INTO Orders "
                + "(orderId, customerId, orderStatus, shippingAddress, phone, placedAt, totalAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String itemQuery = "INSERT INTO OrderItems "
                + "(orderItemId, orderId, variantId, quantity, unitPrice, discountAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(orderQuery)) {
                ps.setString(1, order.getOrderId());
                ps.setString(2, order.getCustomerId());
                ps.setString(3, order.getOrderStatus());
                ps.setString(4, order.getShippingAddress());
                ps.setString(5, order.getPhone());

                if (order.getPlacedAt() == null) {
                    ps.setNull(6, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(6, Timestamp.valueOf(order.getPlacedAt()));
                }

                if (order.getTotalAmount() == null) {
                    ps.setNull(7, Types.DECIMAL);
                } else {
                    ps.setBigDecimal(7, order.getTotalAmount());
                }

                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(itemQuery)) {
                for (OrderItem item : orderItems) {
                    ps.setString(1, item.getOrderItemId());
                    ps.setString(2, item.getOrderId());
                    ps.setString(3, item.getVariantId());
                    ps.setInt(4, item.getQuantity());
                    ps.setBigDecimal(5, item.getUnitPrice());

                    if (item.getDiscountAmount() == null) {
                        ps.setBigDecimal(6, BigDecimal.ZERO);
                    } else {
                        ps.setBigDecimal(6, item.getDiscountAmount());
                    }

                    ps.addBatch();
                }

                ps.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            rollback();
            System.out.println("createOrder error: " + e);
            return false;

        } finally {
            restoreAutoCommit();
        }
    }

    public boolean updateOrderStatus(String orderId, String orderStatus) {
        String query = "UPDATE Orders SET orderStatus = ? WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderStatus);
            ps.setString(2, orderId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("updateOrderStatus error: " + e);
        }

        return false;
    }

    public boolean updateOrderById(String orderId, String orderStatus,
            String shippingAddress, String phone, BigDecimal totalAmount) {

        String query = "UPDATE Orders SET "
                + "orderStatus = ?, "
                + "shippingAddress = ?, "
                + "phone = ?, "
                + "totalAmount = ? "
                + "WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderStatus);
            ps.setString(2, shippingAddress);
            ps.setString(3, phone);
            ps.setBigDecimal(4, totalAmount);
            ps.setString(5, orderId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("updateOrderById error: " + e);
        }

        return false;
    }

    public boolean deleteOrderById(String orderId) {
        String deleteItemsQuery = "DELETE FROM OrderItems WHERE orderId = ?";
        String deleteOrderQuery = "DELETE FROM Orders WHERE orderId = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(deleteItemsQuery)) {
                ps.setString(1, orderId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(deleteOrderQuery)) {
                ps.setString(1, orderId);
                int result = ps.executeUpdate();

                connection.commit();
                return result > 0;
            }

        } catch (SQLException e) {
            rollback();
            System.out.println("deleteOrderById error: " + e);
            return false;

        } finally {
            restoreAutoCommit();
        }
    }

    private Order getOrderFromResultSet(ResultSet rs) throws SQLException {
        Timestamp placedAtTimestamp = rs.getTimestamp("placedAt");
        LocalDateTime placedAt = null;

        if (placedAtTimestamp != null) {
            placedAt = placedAtTimestamp.toLocalDateTime();
        }

        return new Order(
                rs.getString("orderId"),
                rs.getString("customerId"),
                rs.getString("orderStatus"),
                rs.getString("shippingAddress"),
                rs.getString("phone"),
                placedAt,
                rs.getBigDecimal("totalAmount")
        );
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            System.out.println("Rollback failed: " + ex);
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("setAutoCommit error: " + e);
        }
    }
}
