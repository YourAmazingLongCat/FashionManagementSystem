package DALs;

import Models.Bill;
import Models.BillOrderItem;
import Models.CategorySales;
import Models.PaymentMethodStats;
import Models.ProductOption;
import Models.ProductSaleStat;
import Models.ProductSalesRow;
import Models.RevenueStat;
import Models.TopProduct;
import Utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAL for Bill Management.
 *
 * NOTES:
 * - DBContext.getConnection() is INSTANCE (not static), so each query must
 *   create new DBContext() to open a connection, then let try-with-resources
 *   close it after use.
 * - Revenue only counts Bills with paymentStatus = 'Paid'
 *   (Pending/Failed bills haven't actually been collected yet).
 */
public class BillDAO {

    // ================= VIEW / SEARCH / FILTER BILL =================

    /**
     * Get all bills (no filter), newest first.
     */
    public List<Bill> getAllBills() {
        return searchBills(null, null, null, null, null);
    }

    /**
     * Search + filter bills.
     *
     * @param keyword       search by billId, orderId, customer name, phone (nullable/empty)
     * @param paymentStatus filter by payment status (nullable = all)
     * @param orderStatus   filter by order status (nullable = all)
     * @param fromDate      issuedDate >= fromDate (nullable)
     * @param toDate        issuedDate <= toDate 23:59:59 (nullable)
     */
    public List<Bill> searchBills(String keyword, String paymentStatus, String orderStatus,
                                   java.sql.Date fromDate, java.sql.Date toDate) {
        List<Bill> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT b.billId, b.orderId, b.paymentMethod, b.paymentStatus, b.issuedDate, b.totalAmount, "
              + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
              + "       a.fullName AS customerName, a.phone AS customerPhone "
              + "FROM Bills b "
              + "JOIN Orders o ON b.orderId = o.orderId "
              + "JOIN Accounts a ON o.customerId = a.accountId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (b.billId LIKE ? OR b.orderId LIKE ? OR a.fullName LIKE ? OR a.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND b.paymentStatus = ? ");
            params.add(paymentStatus.trim());
        }

        if (orderStatus != null && !orderStatus.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(orderStatus.trim());
        }

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }

        if (toDate != null) {
            // add ~1 day to cover the whole toDate day
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
        }

        sql.append("ORDER BY b.issuedDate DESC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBill(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Count total bills matching the filter (no date filter since Bills have no date).
     */
    public int countBills(String keyword, String paymentStatus, String orderStatus) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Bills b "
              + "JOIN Orders o ON b.orderId = o.orderId "
              + "JOIN Accounts a ON o.customerId = a.accountId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (b.billId LIKE ? OR b.orderId LIKE ? OR a.fullName LIKE ? OR a.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND b.paymentStatus = ? ");
            params.add(paymentStatus.trim());
        }

        if (orderStatus != null && !orderStatus.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(orderStatus.trim());
        }

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Search + filter bills with pagination (no date filter since Bills have no date).
     */
    public List<Bill> searchBillsPaginated(String keyword, String paymentStatus, String orderStatus,
                                            int offset, int limit) {
        List<Bill> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT b.billId, b.orderId, b.paymentMethod, b.paymentStatus, b.issuedDate, b.totalAmount, "
              + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
              + "       a.fullName AS customerName, a.phone AS customerPhone "
              + "FROM Bills b "
              + "JOIN Orders o ON b.orderId = o.orderId "
              + "JOIN Accounts a ON o.customerId = a.accountId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (b.billId LIKE ? OR b.orderId LIKE ? OR a.fullName LIKE ? OR a.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND b.paymentStatus = ? ");
            params.add(paymentStatus.trim());
        }

        if (orderStatus != null && !orderStatus.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(orderStatus.trim());
        }

        sql.append("ORDER BY b.issuedDate DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx++, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBill(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get 1 bill by billId, with customer / order info.
     */
    public Bill getBillById(String billId) {
        String sql = "SELECT b.billId, b.orderId, b.paymentMethod, b.paymentStatus, b.issuedDate, b.totalAmount, "
                   + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
                   + "       a.fullName AS customerName, a.phone AS customerPhone "
                   + "FROM Bills b "
                   + "JOIN Orders o ON b.orderId = o.orderId "
                   + "JOIN Accounts a ON o.customerId = a.accountId "
                   + "WHERE b.billId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, billId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBill(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getString("billId"));
        bill.setOrderId(rs.getString("orderId"));
        bill.setPaymentMethod(rs.getString("paymentMethod"));
        bill.setPaymentStatus(rs.getString("paymentStatus"));
        bill.setIssuedDate(rs.getTimestamp("issuedDate"));
        bill.setTotalAmount(rs.getBigDecimal("totalAmount"));
        bill.setCustomerId(rs.getString("customerId"));
        bill.setOrderStatus(rs.getString("orderStatus"));
        bill.setShippingAddress(rs.getString("shippingAddress"));
        bill.setPlacedAt(rs.getTimestamp("placedAt"));
        bill.setCustomerName(rs.getString("customerName"));
        bill.setCustomerPhone(rs.getString("customerPhone"));
        return bill;
    }

    // ================= VIEW / SEARCH BILL DETAIL =================

    /**
     * Get bill line items (products) for a bill via its orderId.
     */
    public List<BillOrderItem> getBillDetails(String billId) {
        return searchBillDetails(billId, null);
    }

    /**
     * Search bill details by product name / sku within a bill.
     *
     * @param billId  bill to view
     * @param keyword product name or sku (nullable/empty = all)
     */
    public List<BillOrderItem> searchBillDetails(String billId, String keyword) {
        List<BillOrderItem> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT oi.orderItemId, oi.quantity, oi.unitPrice, oi.discountAmount, "
              + "       pv.variantId, pv.sku, "
              + "       p.productId, p.name AS productName, "
              + "       s.sizeName, c.colorName, "
              + "       (SELECT TOP 1 pi.imageUrl FROM ProductImages pi "
              + "         WHERE pi.productId = p.productId AND pi.isPrimary = 1) AS imageUrl "
              + "FROM Bills b "
              + "JOIN OrderItems oi ON oi.orderId = b.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "JOIN Sizes s ON s.sizeId = pv.sizeId "
              + "JOIN Colors c ON c.colorId = pv.colorId "
              + "WHERE b.billId = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(billId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR pv.sku LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        sql.append("ORDER BY p.name");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillOrderItem item = new BillOrderItem();
                    item.setOrderItemId(rs.getString("orderItemId"));
                    item.setVariantId(rs.getString("variantId"));
                    item.setProductId(rs.getString("productId"));
                    item.setProductName(rs.getString("productName"));
                    item.setImageUrl(rs.getString("imageUrl"));
                    item.setSizeName(rs.getString("sizeName"));
                    item.setColorName(rs.getString("colorName"));
                    item.setSku(rs.getString("sku"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unitPrice"));
                    item.setDiscountAmount(rs.getBigDecimal("discountAmount"));
                    list.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= REVENUE CHART =================

    /**
     * Sum revenue by period (day/week/month/year) for the line chart.
     * Only counts bills with paymentStatus = 'Paid'.
     *
     * @param periodType "day" | "week" | "month" | "year" (already validated at Servlet)
     * @param fromDate   nullable
     * @param toDate     nullable
     */
    public List<RevenueStat> getRevenueStats(String periodType, java.sql.Date fromDate, java.sql.Date toDate) {
        List<RevenueStat> list = new ArrayList<>();

        // groupExpr/labelExpr are picked from a fixed whitelist,
        // NOT from user input directly -> avoids SQL Injection.
        String groupExpr;
        String labelExpr;

        switch (periodType) {
            case "week":
                // Group by year + week number
                groupExpr = "DATEPART(year, b.issuedDate), DATEPART(week, b.issuedDate)";
                labelExpr = "CAST(DATEPART(year, b.issuedDate) AS VARCHAR) + '-W' + "
                          + "RIGHT('0' + CAST(DATEPART(week, b.issuedDate) AS VARCHAR), 2)";
                break;
            case "month":
                groupExpr = "FORMAT(b.issuedDate, 'yyyy-MM')";
                labelExpr = "FORMAT(b.issuedDate, 'yyyy-MM')";
                break;
            case "year":
                groupExpr = "DATEPART(year, b.issuedDate)";
                labelExpr = "CAST(DATEPART(year, b.issuedDate) AS VARCHAR)";
                break;
            case "day":
            default:
                groupExpr = "CONVERT(date, b.issuedDate)";
                labelExpr = "CONVERT(varchar(10), b.issuedDate, 23)"; // yyyy-mm-dd
                break;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(labelExpr).append(" AS periodLabel, ")
           .append("       MIN(b.issuedDate) AS sortDate, ")
           .append("       SUM(b.totalAmount) AS totalRevenue, ")
           .append("       COUNT(*) AS billCount ")
           .append("FROM Bills b ")
           .append("WHERE b.paymentStatus = 'Paid' ");

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
        }

        sql.append("GROUP BY ").append(groupExpr).append(" ")
           .append("ORDER BY sortDate ASC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal revenue = rs.getBigDecimal("totalRevenue");
                    list.add(new RevenueStat(
                            rs.getString("periodLabel"),
                            revenue == null ? BigDecimal.ZERO : revenue,
                            rs.getInt("billCount")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= PRODUCT SALES =================

    /**
     * Get products (id + name) for the product dropdown filter.
     */
    public List<ProductOption> getAllProductOptions() {
        List<ProductOption> list = new ArrayList<>();
        String sql = "SELECT productId, name FROM Products ORDER BY name";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ProductOption(rs.getString("productId"), rs.getString("name")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Sales qty + paid revenue by period (day/week/month/year) for the "Products sold" chart.
     * Filter by productId or null/empty = all products.
     * Qty counts ALL orders with bills (any payment status); revenue only counts Paid.
     */
    public List<ProductSaleStat> getProductSalesChart(String periodType, java.sql.Date fromDate,
                                                        java.sql.Date toDate, String productId) {
        List<ProductSaleStat> list = new ArrayList<>();

        String groupExpr;
        String labelExpr;

        switch (periodType) {
            case "week":
                groupExpr = "DATEPART(year, b.issuedDate), DATEPART(week, b.issuedDate)";
                labelExpr = "CAST(DATEPART(year, b.issuedDate) AS VARCHAR) + '-W' + "
                          + "RIGHT('0' + CAST(DATEPART(week, b.issuedDate) AS VARCHAR), 2)";
                break;
            case "year":
                groupExpr = "DATEPART(year, b.issuedDate)";
                labelExpr = "CAST(DATEPART(year, b.issuedDate) AS VARCHAR)";
                break;
            case "day":
                groupExpr = "CONVERT(date, b.issuedDate)";
                labelExpr = "CONVERT(varchar(10), b.issuedDate, 23)";
                break;
            case "month":
            default:
                groupExpr = "FORMAT(b.issuedDate, 'yyyy-MM')";
                labelExpr = "FORMAT(b.issuedDate, 'yyyy-MM')";
                break;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(labelExpr).append(" AS periodLabel, ")
           .append("       MIN(b.issuedDate) AS sortDate, ")
           .append("       SUM(oi.quantity) AS quantitySold, ")
           .append("       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN ")
           .append("            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS revenuePaid ")
           .append("FROM OrderItems oi ")
           .append("JOIN Bills b ON b.orderId = oi.orderId ")
           .append("JOIN ProductVariants pv ON pv.variantId = oi.variantId ")
           .append("JOIN Products p ON p.productId = pv.productId ")
           .append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
        }
        if (productId != null && !productId.trim().isEmpty()) {
            sql.append("AND p.productId = ? ");
            params.add(productId.trim());
        }

        sql.append("GROUP BY ").append(groupExpr).append(" ")
           .append("ORDER BY sortDate ASC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal revenuePaid = rs.getBigDecimal("revenuePaid");
                    list.add(new ProductSaleStat(
                            rs.getString("periodLabel"),
                            rs.getInt("quantitySold"),
                            revenuePaid == null ? BigDecimal.ZERO : revenuePaid
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Per-product summary in current date range:
     * total qty sold, total paid, total remaining (bills != 'Paid').
     * Filter by productId or null/empty = all products.
     */
    public List<ProductSalesRow> getProductSalesSummary(java.sql.Date fromDate, java.sql.Date toDate,
                                                          String productId) {
        List<ProductSalesRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.productId, p.name AS productName, "
              + "       SUM(oi.quantity) AS totalQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN oi.quantity ELSE 0 END) AS paidQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN oi.quantity ELSE 0 END) AS unpaidQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalRevenuePaid, "
              + "       SUM(CASE WHEN b.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalUnpaidAmount "
              + "FROM OrderItems oi "
              + "JOIN Bills b ON b.orderId = oi.orderId "
              + "JOIN Orders o ON o.orderId = oi.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "WHERE o.orderStatus <> 'Cancelled' "
        );

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
        }
        if (productId != null && !productId.trim().isEmpty()) {
            sql.append("AND p.productId = ? ");
            params.add(productId.trim());
        }

        sql.append("GROUP BY p.productId, p.name ")
           .append("ORDER BY totalQuantity DESC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal paid = rs.getBigDecimal("totalRevenuePaid");
                    BigDecimal unpaid = rs.getBigDecimal("totalUnpaidAmount");
                    list.add(new ProductSalesRow(
                            rs.getString("productId"),
                            rs.getString("productName"),
                            rs.getInt("totalQuantity"),
                            rs.getInt("paidQuantity"),
                            rs.getInt("unpaidQuantity"),
                            paid == null ? BigDecimal.ZERO : paid,
                            unpaid == null ? BigDecimal.ZERO : unpaid
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Count Paid vs Unpaid bills containing at least 1 product matching the filter
     * (productId null/empty = all), in the current date range.
     * DISTINCT by billId (one bill can have many OrderItems).
     *
     * @return array of 2: [0] = paid count, [1] = unpaid count
     */
    public int[] getProductOrderCounts(java.sql.Date fromDate, java.sql.Date toDate, String productId) {
        int[] result = new int[]{0, 0};

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT CASE WHEN b.paymentStatus = 'Paid' THEN b.billId END) AS paidCount, "
              + "       COUNT(DISTINCT CASE WHEN b.paymentStatus <> 'Paid' THEN b.billId END) AS unpaidCount "
              + "FROM OrderItems oi "
              + "JOIN Bills b ON b.orderId = oi.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
        }
        if (productId != null && !productId.trim().isEmpty()) {
            sql.append("AND p.productId = ? ");
            params.add(productId.trim());
        }

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getInt("paidCount");
                    result[1] = rs.getInt("unpaidCount");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // ================= INSERT / UPDATE BILL =================

    /**
     * Insert a new Bill into the database.
     *
     * @param bill the Bill object to insert (only DB columns are used)
     * @return true if insert successful
     */
    public boolean insertBill(Bill bill) {
        if (bill == null || isEmpty(bill.getBillId()) || isEmpty(bill.getOrderId())) {
            return false;
        }

        String sql = """
            INSERT INTO Bills (billId, orderId, paymentMethod, paymentStatus, issuedDate, totalAmount)
            VALUES (?, ?, ?, ?, GETDATE(), ?)
        """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bill.getBillId());
            ps.setString(2, bill.getOrderId());
            ps.setString(3, bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "COD");
            ps.setString(4, bill.getPaymentStatus() != null ? bill.getPaymentStatus() : "Pending");
            ps.setBigDecimal(5, bill.getTotalAmount());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertBill error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update the payment status of an existing Bill.
     *
     * @param billId        the billId to update
     * @param paymentStatus new payment status (e.g., Paid, Refunded)
     * @return true if update successful
     */
    public boolean updatePaymentStatus(String billId, String paymentStatus) {
        if (isEmpty(billId) || isEmpty(paymentStatus)) {
            return false;
        }

        String sql = "UPDATE Bills SET paymentStatus = ? WHERE billId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentStatus.trim());
            ps.setString(2, billId.trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updatePaymentStatus error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update payment status by orderId (when order is delivered, payment becomes Paid).
     */
    public boolean updatePaymentStatusByOrderId(String orderId, String paymentStatus) {
        if (isEmpty(orderId) || isEmpty(paymentStatus)) {
            return false;
        }

        String sql = "UPDATE Bills SET paymentStatus = ? WHERE orderId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentStatus.trim());
            ps.setString(2, orderId.trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("updatePaymentStatusByOrderId error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if a Bill already exists for an order.
     *
     * @param orderId the orderId to check
     * @return true if a Bill exists for this order
     */
    public boolean billExistsForOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM Bills WHERE orderId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("billExistsForOrder error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get Bill by orderId.
     */
    public Bill getBillByOrderId(String orderId) {
        if (isEmpty(orderId)) {
            return null;
        }

        String sql = "SELECT b.billId, b.orderId, b.paymentMethod, b.paymentStatus, b.issuedDate, b.totalAmount, "
                   + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
                   + "       a.fullName AS customerName, a.phone AS customerPhone "
                   + "FROM Bills b "
                   + "JOIN Orders o ON b.orderId = o.orderId "
                   + "JOIN Accounts a ON o.customerId = a.accountId "
                   + "WHERE b.orderId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBill(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("getBillByOrderId error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Per-product detail list with Paid/Unpaid filter and sort.
     */
    public List<ProductSalesRow> getProductSalesSummaryByFilter(String paidFilter, String productId, String sortBy) {
        List<ProductSalesRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.productId, p.name AS productName, "
              + "       SUM(oi.quantity) AS totalQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN oi.quantity ELSE 0 END) AS paidQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN oi.quantity ELSE 0 END) AS unpaidQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalRevenuePaid, "
              + "       SUM(CASE WHEN b.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalUnpaidAmount "
              + "FROM OrderItems oi "
              + "JOIN Bills b ON b.orderId = oi.orderId "
              + "JOIN Orders o ON o.orderId = oi.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "WHERE o.orderStatus <> 'Cancelled' "
        );

        List<Object> params = new ArrayList<>();

        if ("Paid".equals(paidFilter)) {
            sql.append("AND b.paymentStatus = 'Paid' ");
        } else if ("Unpaid".equals(paidFilter)) {
            sql.append("AND b.paymentStatus <> 'Paid' ");
        }

        if (productId != null && !productId.trim().isEmpty()) {
            sql.append("AND p.productId = ? ");
            params.add(productId.trim());
        }

        sql.append("GROUP BY p.productId, p.name ");

        if ("asc".equals(sortBy)) {
            sql.append("ORDER BY totalUnpaidAmount ASC");
        } else if ("desc".equals(sortBy)) {
            sql.append("ORDER BY totalUnpaidAmount DESC");
        } else {
            sql.append("ORDER BY totalQuantity DESC");
        }

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal paid = rs.getBigDecimal("totalRevenuePaid");
                    BigDecimal unpaid = rs.getBigDecimal("totalUnpaidAmount");
                    list.add(new ProductSalesRow(
                            rs.getString("productId"),
                            rs.getString("productName"),
                            rs.getInt("totalQuantity"),
                            rs.getInt("paidQuantity"),
                            rs.getInt("unpaidQuantity"),
                            paid == null ? BigDecimal.ZERO : paid,
                            unpaid == null ? BigDecimal.ZERO : unpaid
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ================= TOP SELLING PRODUCTS =================

    /**
     * Get top selling products by quantity sold.
     */
    public List<TopProduct> getTopSellingProducts(int limit) {
        List<TopProduct> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) p.productId, p.name AS productName,
                   (SELECT TOP 1 pi.imageUrl FROM ProductImages pi
                    WHERE pi.productId = p.productId AND pi.isPrimary = 1) AS imageUrl,
                   SUM(oi.quantity) AS totalSold
            FROM OrderItems oi
            JOIN Bills b ON b.orderId = oi.orderId
            JOIN ProductVariants pv ON pv.variantId = oi.variantId
            JOIN Products p ON p.productId = pv.productId
            WHERE b.paymentStatus = 'Paid'
            GROUP BY p.productId, p.name
            ORDER BY totalSold DESC
        """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopProduct(
                            rs.getString("productId"),
                            rs.getString("productName"),
                            rs.getString("imageUrl"),
                            rs.getInt("totalSold"),
                            0
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get top revenue products.
     */
    public List<TopProduct> getTopRevenueProducts(int limit) {
        List<TopProduct> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) p.productId, p.name AS productName,
                   (SELECT TOP 1 pi.imageUrl FROM ProductImages pi
                    WHERE pi.productId = p.productId AND pi.isPrimary = 1) AS imageUrl,
                   SUM(oi.quantity) AS totalSold,
                   SUM(oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) AS totalRevenue
            FROM OrderItems oi
            JOIN Bills b ON b.orderId = oi.orderId
            JOIN ProductVariants pv ON pv.variantId = oi.variantId
            JOIN Products p ON p.productId = pv.productId
            WHERE b.paymentStatus = 'Paid'
            GROUP BY p.productId, p.name
            ORDER BY totalRevenue DESC
        """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopProduct(
                            rs.getString("productId"),
                            rs.getString("productName"),
                            rs.getString("imageUrl"),
                            rs.getInt("totalSold"),
                            rs.getInt("totalRevenue")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= SALES BY CATEGORY =================

    /**
     * Get sales statistics grouped by category.
     */
    public List<CategorySales> getSalesByCategory() {
        List<CategorySales> list = new ArrayList<>();
        String sql = """
            SELECT c.categoryId, c.name AS categoryName,
                   SUM(oi.quantity) AS totalSold,
                   SUM(CASE WHEN b.paymentStatus = 'Paid'
                        THEN oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)
                        ELSE 0 END) AS totalRevenue
            FROM OrderItems oi
            JOIN Bills b ON b.orderId = oi.orderId
            JOIN ProductVariants pv ON pv.variantId = oi.variantId
            JOIN Products p ON p.productId = pv.productId
            JOIN Categories c ON c.categoryId = p.categoryId
            GROUP BY c.categoryId, c.name
            ORDER BY totalRevenue DESC
        """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CategorySales(
                        rs.getString("categoryId"),
                        rs.getString("categoryName"),
                        rs.getInt("totalSold"),
                        rs.getBigDecimal("totalRevenue")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= SALES BY PAYMENT METHOD =================

    /**
     * Get sales statistics grouped by payment method.
     */
    public List<PaymentMethodStats> getSalesByPaymentMethod() {
        List<PaymentMethodStats> list = new ArrayList<>();
        String sql = """
            SELECT b.paymentMethod,
                   COUNT(*) AS totalBills,
                   SUM(b.totalAmount) AS totalRevenue
            FROM Bills b
            WHERE b.paymentStatus = 'Paid'
            GROUP BY b.paymentMethod
            ORDER BY totalRevenue DESC
        """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PaymentMethodStats(
                        rs.getString("paymentMethod"),
                        rs.getInt("totalBills"),
                        rs.getBigDecimal("totalRevenue")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}