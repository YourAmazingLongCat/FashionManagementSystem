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
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class OrderDAO extends DBContext {

    public OrderDAO() {
        super();
    }

    public List<Order> getAllOrders() {
        List<Order> listOrders = new ArrayList<>();
        String query = "SELECT o.*, CAST(CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.orderId = o.orderId AND p.paymentType = 'Purchase') THEN 1 ELSE 0 END AS BIT) AS orderPlaced FROM Orders o ORDER BY o.placedAt DESC";

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
        String query = "SELECT o.*, CAST(CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.orderId = o.orderId AND p.paymentType = 'Purchase') THEN 1 ELSE 0 END AS BIT) AS orderPlaced FROM Orders o WHERE o.orderId = ?";

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
        String query = "SELECT o.*, CAST(CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.orderId = o.orderId AND p.paymentType = 'Purchase') THEN 1 ELSE 0 END AS BIT) AS orderPlaced FROM Orders o WHERE o.orderId = ? AND o.customerId = ?";

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
        String query = "SELECT o.*, CAST(CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.orderId = o.orderId AND p.paymentType = 'Purchase') THEN 1 ELSE 0 END AS BIT) AS orderPlaced FROM Orders o WHERE o.customerId = ? ORDER BY o.placedAt DESC";

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

        String query = "SELECT o.*, CAST(CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.orderId = o.orderId AND p.paymentType = 'Purchase') THEN 1 ELSE 0 END AS BIT) AS orderPlaced FROM Orders o "
                + "WHERE o.customerId = ? "
                + "AND (o.orderId LIKE ? OR o.orderStatus LIKE ? OR o.phone LIKE ? OR o.shippingAddress LIKE ?) "
                + "ORDER BY o.placedAt DESC";

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

        String query = "SELECT o.*, CAST(CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.orderId = o.orderId AND p.paymentType = 'Purchase') THEN 1 ELSE 0 END AS BIT) AS orderPlaced FROM Orders o "
                + "WHERE o.orderId LIKE ? "
                + "OR o.customerId LIKE ? "
                + "OR o.orderStatus LIKE ? "
                + "OR o.phone LIKE ? "
                + "OR o.shippingAddress LIKE ? "
                + "ORDER BY o.placedAt DESC";

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
        return createOrderInternal(order, orderItems, null, null);
    }

    /**
     * Creates a Pending order, reserves its stock and removes the selected
     * cart rows in one database transaction. This is the entry point used by
     * the Cart Checkout button, so the cart cannot remain unchanged after an
     * order has already been created.
     */
    public boolean createOrderFromCart(Order order, List<OrderItem> orderItems,
            String cartId, String[] cartItemIds) {
        if (isBlank(cartId) || cartItemIds == null || cartItemIds.length == 0) {
            return false;
        }

        Set<String> uniqueItemIds = new LinkedHashSet<>();
        for (String itemId : cartItemIds) {
            if (!isBlank(itemId)) {
                uniqueItemIds.add(itemId.trim());
            }
        }

        if (uniqueItemIds.isEmpty()) {
            return false;
        }

        return createOrderInternal(order, orderItems, cartId.trim(),
                new ArrayList<>(uniqueItemIds));
    }

    private boolean createOrderInternal(Order order, List<OrderItem> orderItems,
            String cartId, List<String> cartItemIds) {
        if (order == null || orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        String orderQuery = "INSERT INTO Orders "
                + "(orderId, customerId, orderStatus, shippingAddress, phone, placedAt, totalAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String reserveQuery = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET reservedQty = reservedQty + ? "
                + "WHERE variantId = ? AND (stockQty - reservedQty) >= ?";

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

            /*
             * Reserve every variant inside the same database transaction that
             * creates the Order. The conditional UPDATE is atomic, so two
             * customers cannot both reserve the last available unit.
             */
            Map<String, Integer> quantitiesByVariant = aggregateQuantities(orderItems);
            try (PreparedStatement ps = connection.prepareStatement(reserveQuery)) {
                for (Map.Entry<String, Integer> entry : quantitiesByVariant.entrySet()) {
                    int quantity = entry.getValue();
                    ps.setInt(1, quantity);
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, quantity);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Insufficient available stock for variant " + entry.getKey());
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(itemQuery)) {
                for (OrderItem item : orderItems) {
                    ps.setString(1, item.getOrderItemId());
                    ps.setString(2, item.getOrderId());
                    ps.setString(3, item.getVariantId());
                    ps.setInt(4, item.getQuantity());
                    ps.setBigDecimal(5, item.getUnitPrice());
                    ps.setBigDecimal(6, item.getDiscountAmount() == null
                            ? BigDecimal.ZERO : item.getDiscountAmount());
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            if (cartId != null && cartItemIds != null && !cartItemIds.isEmpty()) {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < cartItemIds.size(); i++) {
                    if (i > 0) {
                        placeholders.append(',');
                    }
                    placeholders.append('?');
                }

                String deleteCartSql = "DELETE FROM CartItems "
                        + "WHERE cartId = ? AND cartItemId IN (" + placeholders + ")";

                try (PreparedStatement ps = connection.prepareStatement(deleteCartSql)) {
                    ps.setString(1, cartId);
                    int parameterIndex = 2;
                    for (String itemId : cartItemIds) {
                        ps.setString(parameterIndex++, itemId);
                    }

                    int deletedRows = ps.executeUpdate();
                    if (deletedRows != cartItemIds.size()) {
                        throw new SQLException("The selected cart changed before checkout completed.");
                    }
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            rollback();
            System.out.println("createOrder error: " + e.getMessage());
            return false;

        } finally {
            restoreAutoCommit();
        }
    }


    /**
     * Changes an order status and performs the matching inventory transition
     * in one transaction.
     *
     * Pending -> Confirmed: reserved stock becomes sold stock.
     * Confirmed -> Pending: sold stock returns to a reservation.
     * Other one-step status changes do not alter inventory.
     */
    public boolean changeOrderStatusWithInventory(String orderId,
            String expectedCurrentStatus, String newStatus) {
        if (isBlank(orderId) || isBlank(expectedCurrentStatus) || isBlank(newStatus)) {
            return false;
        }

        String lockOrderSql = "SELECT orderStatus FROM Orders WITH (UPDLOCK, ROWLOCK) WHERE orderId = ?";
        String updateOrderSql = "UPDATE Orders SET orderStatus = ? WHERE orderId = ? AND orderStatus = ?";

        try {
            connection.setAutoCommit(false);

            String currentStatus;
            try (PreparedStatement ps = connection.prepareStatement(lockOrderSql)) {
                ps.setString(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rollback();
                        return false;
                    }
                    currentStatus = rs.getString("orderStatus");
                }
            }

            if (!expectedCurrentStatus.equals(currentStatus)) {
                rollback();
                return false;
            }

            if ("Pending".equals(currentStatus) && "Confirmed".equals(newStatus)) {
                if (!commitReservedStock(orderId)) {
                    rollback();
                    return false;
                }
            } else if ("Confirmed".equals(currentStatus) && "Pending".equals(newStatus)) {
                if (!moveCommittedStockBackToReservation(orderId)) {
                    rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(updateOrderSql)) {
                ps.setString(1, newStatus);
                ps.setString(2, orderId);
                ps.setString(3, expectedCurrentStatus);
                if (ps.executeUpdate() != 1) {
                    rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback();
            System.out.println("changeOrderStatusWithInventory error: " + e.getMessage());
            return false;
        } finally {
            restoreAutoCommit();
        }
    }

    /**
     * Cancels an order and restores/release inventory consistently.
     * Pending orders release reservedQty. Confirmed/Processing orders have
     * already consumed physical stock, so that stock is returned.
     */
    public boolean cancelOrderAndAdjustInventory(String orderId) {
        if (isBlank(orderId)) {
            return false;
        }

        String lockOrderSql = "SELECT orderStatus FROM Orders WITH (UPDLOCK, ROWLOCK) WHERE orderId = ?";
        String cancelSql = "UPDATE Orders SET orderStatus = 'Cancelled' WHERE orderId = ? AND orderStatus = ?";

        try {
            connection.setAutoCommit(false);

            String currentStatus;
            try (PreparedStatement ps = connection.prepareStatement(lockOrderSql)) {
                ps.setString(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rollback();
                        return false;
                    }
                    currentStatus = rs.getString("orderStatus");
                }
            }

            if ("Pending".equals(currentStatus)) {
                if (!releaseReservedStock(orderId)) {
                    rollback();
                    return false;
                }
            } else if ("Confirmed".equals(currentStatus) || "Processing".equals(currentStatus)) {
                if (!restoreCommittedStock(orderId)) {
                    rollback();
                    return false;
                }
            } else {
                rollback();
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(cancelSql)) {
                ps.setString(1, orderId);
                ps.setString(2, currentStatus);
                if (ps.executeUpdate() != 1) {
                    rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback();
            System.out.println("cancelOrderAndAdjustInventory error: " + e.getMessage());
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

    /**
     * Allows the owning customer to update delivery details only while the
     * order has not started shipping. The status condition is part of the
     * UPDATE statement to avoid a race with staff changing the status.
     */
    public boolean updateDeliveryInformationForCustomer(String orderId,
            String customerId, String shippingAddress, String phone) {
        String query = "UPDATE Orders SET shippingAddress = ?, phone = ? "
                + "WHERE orderId = ? AND customerId = ? "
                + "AND orderStatus IN ('Pending', 'Confirmed', 'Processing')";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, shippingAddress);
            ps.setString(2, phone);
            ps.setString(3, orderId);
            ps.setString(4, customerId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("updateDeliveryInformationForCustomer error: " + e.getMessage());
            return false;
        }
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

        Order order = new Order(
                rs.getString("orderId"),
                rs.getString("customerId"),
                rs.getString("orderStatus"),
                rs.getString("shippingAddress"),
                rs.getString("phone"),
                placedAt,
                rs.getBigDecimal("totalAmount")
        );
        order.setOrderPlaced(rs.getBoolean("orderPlaced"));
        return order;
    }


    private Map<String, Integer> aggregateQuantities(List<OrderItem> orderItems) {
        Map<String, Integer> quantities = new TreeMap<>();
        for (OrderItem item : orderItems) {
            quantities.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    private Map<String, Integer> getOrderQuantities(String orderId) throws SQLException {
        Map<String, Integer> quantities = new TreeMap<>();
        String sql = "SELECT variantId, SUM(quantity) AS quantity "
                + "FROM OrderItems WHERE orderId = ? GROUP BY variantId ORDER BY variantId";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quantities.put(rs.getString("variantId"), rs.getInt("quantity"));
                }
            }
        }
        return quantities;
    }

    private boolean commitReservedStock(String orderId) throws SQLException {
        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET stockQty = stockQty - ?, reservedQty = reservedQty - ? "
                + "WHERE variantId = ? AND stockQty >= ? AND reservedQty >= ?";
        return updateInventoryForOrder(orderId, sql, InventoryOperation.COMMIT_RESERVED);
    }

    private boolean releaseReservedStock(String orderId) throws SQLException {
        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET reservedQty = reservedQty - ? "
                + "WHERE variantId = ? AND reservedQty >= ?";
        return updateInventoryForOrder(orderId, sql, InventoryOperation.RELEASE_RESERVED);
    }

    private boolean restoreCommittedStock(String orderId) throws SQLException {
        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET stockQty = stockQty + ? WHERE variantId = ?";
        return updateInventoryForOrder(orderId, sql, InventoryOperation.RESTORE_COMMITTED);
    }

    private boolean moveCommittedStockBackToReservation(String orderId) throws SQLException {
        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET stockQty = stockQty + ?, reservedQty = reservedQty + ? "
                + "WHERE variantId = ?";
        return updateInventoryForOrder(orderId, sql, InventoryOperation.MOVE_BACK_TO_RESERVED);
    }

    private boolean updateInventoryForOrder(String orderId, String sql,
            InventoryOperation operation) throws SQLException {
        Map<String, Integer> quantities = getOrderQuantities(orderId);
        if (quantities.isEmpty()) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
                int quantity = entry.getValue();
                String variantId = entry.getKey();

                switch (operation) {
                    case COMMIT_RESERVED:
                        ps.setInt(1, quantity);
                        ps.setInt(2, quantity);
                        ps.setString(3, variantId);
                        ps.setInt(4, quantity);
                        ps.setInt(5, quantity);
                        break;
                    case RELEASE_RESERVED:
                        ps.setInt(1, quantity);
                        ps.setString(2, variantId);
                        ps.setInt(3, quantity);
                        break;
                    case RESTORE_COMMITTED:
                        ps.setInt(1, quantity);
                        ps.setString(2, variantId);
                        break;
                    case MOVE_BACK_TO_RESERVED:
                        ps.setInt(1, quantity);
                        ps.setInt(2, quantity);
                        ps.setString(3, variantId);
                        break;
                    default:
                        return false;
                }

                if (ps.executeUpdate() != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private enum InventoryOperation {
        COMMIT_RESERVED,
        RELEASE_RESERVED,
        RESTORE_COMMITTED,
        MOVE_BACK_TO_RESERVED
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
