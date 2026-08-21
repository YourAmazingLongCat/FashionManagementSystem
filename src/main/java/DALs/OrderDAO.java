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

    /**
     * Count orders whose status is still Pending that reference any variant of
     * the given product. Used to block product deletion only while the order
     * is in Pending state.
     */
    public int countPendingOrdersByProductId(String productId) {
        if (connection == null || productId == null || productId.isBlank()) return 0;
        String sql = "SELECT COUNT(*) FROM OrderItems oi "
                + "JOIN Orders o ON oi.orderId = o.orderId "
                + "JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "WHERE pv.productId = ? AND o.orderStatus = 'Pending'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println("countPendingOrdersByProductId error: " + ex.getMessage());
        }
        return 0;
    }

    public int countOrders(String keyword) {
        return countOrders(keyword, null, null, null);
    }

    public int countOrders(String keyword, String status, LocalDateTime dateFrom, LocalDateTime dateTo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Orders o WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.orderId LIKE ? OR o.shippingPhone LIKE ? OR o.shippingAddress LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(status.trim());
        }
        if (dateFrom != null) {
            sql.append("AND o.placedAt >= ? ");
            params.add(Timestamp.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append("AND o.placedAt < ? ");
            params.add(Timestamp.valueOf(dateTo));
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("countOrders error: " + e);
        }
        return 0;
    }

    /**
     * "orderPlaced" now means Orders has a non-null paymentMethod AND a
     * non-Pending WalletTransactions row. To stay simple, we check that the
     * Orders row already has a payment method recorded (PaymentService mirrors
     * the payment method onto Orders whenever a Purchase transaction is
     * created).
     */
    private static final String ORDER_PLACED_EXPR
            = "CAST(CASE WHEN o.paymentMethod IS NOT NULL "
            + "          AND EXISTS (SELECT 1 FROM WalletTransactions wt "
            + "                       WHERE wt.orderId = o.orderId "
            + "                         AND wt.transactionType = 'Purchase') "
            + "          THEN 1 ELSE 0 END AS BIT)";

    public List<Order> getOrdersPaginated(String keyword, int offset, int limit) {
        return getOrdersPaginated(keyword, null, null, null, offset, limit);
    }

    public List<Order> getOrdersPaginated(String keyword, String status, LocalDateTime dateFrom, LocalDateTime dateTo, int offset, int limit) {
        List<Order> listOrders = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.orderId LIKE ? OR o.customerId LIKE ? OR o.shippingPhone LIKE ? OR o.shippingAddress LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(status.trim());
        }
        if (dateFrom != null) {
            sql.append("AND o.placedAt >= ? ");
            params.add(Timestamp.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append("AND o.placedAt < ? ");
            params.add(Timestamp.valueOf(dateTo));
        }

        sql.append("ORDER BY o.placedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listOrders.add(getOrderFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getOrdersPaginated error: " + e);
        }
        return listOrders;
    }

    public List<Order> getAllOrders() {
        List<Order> listOrders = new ArrayList<>();
        String query = "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o ORDER BY o.placedAt DESC";

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
        String query = "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o WHERE o.orderId = ?";

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
        String query = "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o WHERE o.orderId = ? AND o.customerId = ?";

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
        String query = "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o WHERE o.customerId = ? ORDER BY o.placedAt DESC";

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

        String query = "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o "
                + "WHERE o.customerId = ? "
                + "AND (o.orderId LIKE ? OR o.orderStatus LIKE ? OR o.shippingPhone LIKE ? OR o.shippingAddress LIKE ?) "
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

    public List<Order> searchOrdersPaginated(String keyword, int offset, int limit) {
        return searchOrdersPaginated(keyword, null, null, null, offset, limit);
    }

    public List<Order> searchOrdersPaginated(String keyword, String status, LocalDateTime dateFrom, LocalDateTime dateTo, int offset, int limit) {
        List<Order> listOrders = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.orderId LIKE ? OR o.customerId LIKE ? OR o.orderStatus LIKE ? OR o.shippingPhone LIKE ? OR o.shippingAddress LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(status.trim());
        }
        if (dateFrom != null) {
            sql.append("AND o.placedAt >= ? ");
            params.add(Timestamp.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append("AND o.placedAt < ? ");
            params.add(Timestamp.valueOf(dateTo));
        }

        sql.append("ORDER BY o.placedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listOrders.add(getOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("searchOrdersPaginated error: " + e);
        }

        return listOrders;
    }

    public List<Order> searchOrdersForStaff(String keyword) {
        List<Order> listOrders = new ArrayList<>();

        String query = "SELECT o.*, " + ORDER_PLACED_EXPR + " AS orderPlaced FROM Orders o "
                + "WHERE o.orderId LIKE ? "
                + "OR o.customerId LIKE ? "
                + "OR o.orderStatus LIKE ? "
                + "OR o.shippingPhone LIKE ? "
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
     * cart rows in one database transaction.
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

        // New schema: phone -> shippingPhone. Order has no separate phone column.
        String orderQuery = "INSERT INTO Orders "
                + "(orderId, customerId, orderStatus, shippingAddress, shippingPhone, placedAt, totalAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Stock is intentionally NOT touched at checkout time. The Pending
        // order just records what the customer wants to buy; reserving or
        // deducting stock here would make the variant look out-of-stock to
        // other shoppers even though the customer hasn't actually placed
        // the order yet. Reservation only happens when the customer presses
        // Place order (see CustomerOrderDetailServlet#placeOrder), and the
        // real deduction happens when staff confirms the order.
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
                    ps.setBigDecimal(6, item.getDiscountAmount() == null
                            ? BigDecimal.ZERO : item.getDiscountAmount());
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            if (cartId != null && cartItemIds != null && !cartItemIds.isEmpty()) {
                // In the new schema Cart rows are keyed by cartId (PK).
                // The caller passes the cartIds of the rows to remove.
                String deleteCartSql = "DELETE FROM Cart WHERE customerId = ? AND cartId IN ("
                        + placeholders(cartItemIds.size()) + ")";

                try (PreparedStatement ps = connection.prepareStatement(deleteCartSql)) {
                    ps.setString(1, order.getCustomerId());
                    int parameterIndex = 2;
                    for (String cartRowId : cartItemIds) {
                        ps.setString(parameterIndex++, cartRowId);
                    }

                    int deletedRows = ps.executeUpdate();
                    // Loose match — the cart may have changed since selection.
                    // We only fail if the customer has no cart at all to clean up.
                    if (deletedRows == 0 && !hasAnyCartRow(connection, order.getCustomerId())) {
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
     * Reserves stock for an existing Pending order. Called when the customer
     * presses Place order on the order detail page. Stock is intentionally
     * not touched at Cart Checkout (see {@link #createOrderFromCart}); this
     * method closes the gap so the chosen variants are no longer visible to
     * other shoppers.
     */
    public boolean reserveStockForOrder(String orderId) {
        if (isBlank(orderId)) {
            return false;
        }

        Map<String, Integer> quantities;
        try {
            quantities = getOrderQuantities(orderId);
        } catch (SQLException ex) {
            System.out.println("reserveStockForOrder: failed to load order quantities - " + ex.getMessage());
            return false;
        }
        if (quantities.isEmpty()) {
            return false;
        }

        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET reservedQty = reservedQty + ? "
                + "WHERE variantId = ? AND (stockQty - reservedQty) >= ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
                    int quantity = entry.getValue();
                    ps.setInt(1, quantity);
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, quantity);

                    if (ps.executeUpdate() != 1) {
                        rollback();
                        System.out.println("reserveStockForOrder: insufficient available stock for variant "
                                + entry.getKey() + " in order " + orderId);
                        return false;
                    }
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            rollback();
            System.out.println("reserveStockForOrder error: " + e.getMessage());
            return false;
        } finally {
            restoreAutoCommit();
        }
    }

    private static String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        return sb.toString();
    }

    private static boolean hasAnyCartRow(java.sql.Connection conn, String customerId) throws SQLException {
        String sql = "SELECT 1 FROM Cart WHERE customerId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Changes an order status and performs the matching inventory transition
     * in one transaction.
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

            if (isForbiddenTransitionFromShippingOrLater(currentStatus, newStatus)) {
                rollback();
                return false;
            }

            if ("Pending".equals(currentStatus) && "Confirmed".equals(newStatus)) {
                boolean hasReservedStock = hasReservedStock(orderId);
                if (hasReservedStock) {
                    if (!commitReservedStock(orderId)) {
                        rollback();
                        return false;
                    }
                } else {
                    if (!deductStockForConfirm(orderId)) {
                        rollback();
                        return false;
                    }
                }
                // Bills no longer exist - paymentMethod/paymentStatus live on
                // the Orders row directly (PaymentService fills them in).
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

    private boolean isForbiddenTransitionFromShippingOrLater(String currentStatus, String newStatus) {
        int currentIndex = getProgressStatusIndex(currentStatus);
        int newIndex = getProgressStatusIndex(newStatus);
        int shippingIndex = getProgressStatusIndex("Shipping");

        if (currentIndex < shippingIndex) {
            return false;
        }

        return newIndex < currentIndex;
    }

    private int getProgressStatusIndex(String status) {
        if ("Pending".equals(status)) {
            return 0;
        }
        if ("Confirmed".equals(status)) {
            return 1;
        }
        if ("Processing".equals(status)) {
            return 2;
        }
        if ("Shipping".equals(status)) {
            return 3;
        }
        if ("Delivered".equals(status)) {
            return 4;
        }
        return -1;
    }

    public boolean updateOrderStatus(String orderId, String orderStatus) {
        String query = "UPDATE Orders SET orderStatus = ? WHERE orderId = ? "
                + "AND NOT ((orderStatus = 'Shipping' AND ? NOT IN ('Shipping', 'Delivered')) "
                + "OR (orderStatus = 'Delivered' AND ? <> 'Delivered'))";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderStatus);
            ps.setString(2, orderId);
            ps.setString(3, orderStatus);
            ps.setString(4, orderStatus);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("updateOrderStatus error: " + e);
        }

        return false;
    }

    public boolean updateDeliveryInformationForCustomer(String orderId,
            String customerId, String shippingAddress, String phone) {
        String query = "UPDATE Orders SET shippingAddress = ?, shippingPhone = ? "
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
                + "shippingPhone = ?, "
                + "totalAmount = ? "
                + "WHERE orderId = ? "
                + "AND NOT ((orderStatus = 'Shipping' AND ? NOT IN ('Shipping', 'Delivered')) "
                + "OR (orderStatus = 'Delivered' AND ? <> 'Delivered'))";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, orderStatus);
            ps.setString(2, shippingAddress);
            ps.setString(3, phone);
            ps.setBigDecimal(4, totalAmount);
            ps.setString(5, orderId);
            ps.setString(6, orderStatus);
            ps.setString(7, orderStatus);

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

        // New schema: shippingPhone replaces phone.
        Order order = new Order(
                rs.getString("orderId"),
                rs.getString("customerId"),
                rs.getString("orderStatus"),
                rs.getString("shippingAddress"),
                rs.getString("shippingPhone"),
                placedAt,
                rs.getBigDecimal("totalAmount")
        );
        order.setOrderPlaced(rs.getBoolean("orderPlaced"));
        return order;
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
        // Lenient version: a Pending order created via Cart Checkout before
        // Place order no longer holds any reservation. Releasing zero is a
        // no-op rather than an error so cancellation still succeeds.
        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET reservedQty = reservedQty - ? "
                + "WHERE variantId = ? AND reservedQty >= ?";
        return updateInventoryForOrderLenient(orderId, sql, InventoryOperation.RELEASE_RESERVED);
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
        return updateInventoryForOrderInternal(orderId, sql, operation, false);
    }

    /**
     * Same as {@link #updateInventoryForOrder} but treats an affected-row
     * count of 0 as success. Used by release flows where stock may have
     * never been reserved (e.g. a Pending order that never reached Place
     * order under the new reservation flow).
     */
    private boolean updateInventoryForOrderLenient(String orderId, String sql,
            InventoryOperation operation) throws SQLException {
        return updateInventoryForOrderInternal(orderId, sql, operation, true);
    }

    private boolean updateInventoryForOrderInternal(String orderId, String sql,
            InventoryOperation operation, boolean lenient) throws SQLException {
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

                int affected = ps.executeUpdate();
                if (!lenient && affected != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasReservedStock(String orderId) throws SQLException {
        String sql = "SELECT 1 FROM OrderItems oi "
                + "INNER JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "WHERE oi.orderId = ? AND pv.reservedQty >= oi.quantity";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        String checkSql = "SELECT 1 FROM OrderItems oi "
                + "INNER JOIN ProductVariants pv ON oi.variantId = pv.variantId "
                + "WHERE oi.orderId = ? AND pv.reservedQty > 0";
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean deductStockForConfirm(String orderId) throws SQLException {
        String sql = "UPDATE ProductVariants WITH (UPDLOCK, ROWLOCK) "
                + "SET stockQty = stockQty - oi.quantity "
                + "FROM ProductVariants pv "
                + "INNER JOIN OrderItems oi ON pv.variantId = oi.variantId "
                + "WHERE oi.orderId = ? AND (pv.stockQty - pv.reservedQty) >= oi.quantity";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId);
            int updated = ps.executeUpdate();
            System.out.println("deductStockForConfirm: updated " + updated + " rows for orderId=" + orderId);
            return updated > 0;
        }
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