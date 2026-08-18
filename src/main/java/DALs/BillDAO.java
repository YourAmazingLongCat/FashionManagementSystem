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
 * The schema no longer has a separate Bills table. Bills/invoices are read
 * straight out of Orders (each order carries paymentMethod, paymentStatus,
 * paidAmount, totalAmount, issuedDate). Every public method returns the same
 * {@link Bill} shape the JSP layer already understands.
 *
 * Revenue only counts Orders with paymentStatus = 'Paid'.
 */
public class BillDAO {

    // ================= VIEW / SEARCH / FILTER BILL =================

    public List<Bill> getAllBills() {
        return searchBills(null, null, null, null, null);
    }

    public List<Bill> searchBills(String keyword, String paymentStatus, String orderStatus,
                                   java.sql.Date fromDate, java.sql.Date toDate) {
        List<Bill> list = new ArrayList<>();

        // billId is synthesised as the orderId so callers can still key by id.
        StringBuilder sql = new StringBuilder(
                "SELECT o.orderId AS billId, o.orderId, o.paymentMethod, o.paymentStatus, "
              + "       o.issuedDate, o.totalAmount, "
              + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
              + "       c.fullName AS customerName, c.phone AS customerPhone "
              + "FROM Orders o "
              + "JOIN Customers c ON o.customerId = c.customerId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.orderId LIKE ? OR c.fullName LIKE ? OR c.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND o.paymentStatus = ? ");
            params.add(paymentStatus.trim());
        }

        if (orderStatus != null && !orderStatus.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(orderStatus.trim());
        }

        if (fromDate != null) {
            sql.append("AND o.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }

        if (toDate != null) {
            sql.append("AND o.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
        }

        sql.append("ORDER BY o.issuedDate DESC");

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

    public int countBills(String keyword, String paymentStatus, String orderStatus) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Orders o "
              + "JOIN Customers c ON o.customerId = c.customerId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.orderId LIKE ? OR c.fullName LIKE ? OR c.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND o.paymentStatus = ? ");
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

    public List<Bill> searchBillsPaginated(String keyword, String paymentStatus, String orderStatus,
                                            int offset, int limit) {
        List<Bill> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT o.orderId AS billId, o.orderId, o.paymentMethod, o.paymentStatus, "
              + "       o.issuedDate, o.totalAmount, "
              + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
              + "       c.fullName AS customerName, c.phone AS customerPhone "
              + "FROM Orders o "
              + "JOIN Customers c ON o.customerId = c.customerId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.orderId LIKE ? OR c.fullName LIKE ? OR c.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            sql.append("AND o.paymentStatus = ? ");
            params.add(paymentStatus.trim());
        }

        if (orderStatus != null && !orderStatus.trim().isEmpty()) {
            sql.append("AND o.orderStatus = ? ");
            params.add(orderStatus.trim());
        }

        sql.append("ORDER BY o.issuedDate DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, limit);

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

    public Bill getBillById(String billId) {
        String sql = "SELECT o.orderId AS billId, o.orderId, o.paymentMethod, o.paymentStatus, "
                   + "       o.issuedDate, o.totalAmount, "
                   + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
                   + "       c.fullName AS customerName, c.phone AS customerPhone "
                   + "FROM Orders o "
                   + "JOIN Customers c ON o.customerId = c.customerId "
                   + "WHERE o.orderId = ?";

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

    public List<BillOrderItem> getBillDetails(String billId) {
        return searchBillDetails(billId, null);
    }

    public List<BillOrderItem> searchBillDetails(String billId, String keyword) {
        List<BillOrderItem> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT oi.orderItemId, oi.quantity, oi.unitPrice, oi.discountAmount, "
              + "       pv.variantId, pv.sku, "
              + "       p.productId, p.name AS productName, "
              + "       s.sizeName, c.colorName, "
              + "       (SELECT TOP 1 pi.imageUrl FROM ProductImages pi "
              + "         WHERE pi.productId = p.productId AND pi.isPrimary = 1) AS imageUrl "
              + "FROM Orders o "
              + "JOIN OrderItems oi ON oi.orderId = o.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "JOIN Sizes s ON s.sizeId = pv.sizeId "
              + "JOIN Colors c ON c.colorId = pv.colorId "
              + "WHERE o.orderId = ? "
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

    public List<RevenueStat> getRevenueStats(String periodType, java.sql.Date fromDate, java.sql.Date toDate) {
        List<RevenueStat> list = new ArrayList<>();

        String groupExpr;
        String labelExpr;

        switch (periodType) {
            case "week":
                groupExpr = "DATEPART(year, o.issuedDate), DATEPART(week, o.issuedDate)";
                labelExpr = "CAST(DATEPART(year, o.issuedDate) AS VARCHAR) + '-W' + "
                          + "RIGHT('0' + CAST(DATEPART(week, o.issuedDate) AS VARCHAR), 2)";
                break;
            case "month":
                groupExpr = "FORMAT(o.issuedDate, 'yyyy-MM')";
                labelExpr = "FORMAT(o.issuedDate, 'yyyy-MM')";
                break;
            case "year":
                groupExpr = "DATEPART(year, o.issuedDate)";
                labelExpr = "CAST(DATEPART(year, o.issuedDate) AS VARCHAR)";
                break;
            case "day":
            default:
                groupExpr = "CONVERT(date, o.issuedDate)";
                labelExpr = "CONVERT(varchar(10), o.issuedDate, 23)"; // yyyy-mm-dd
                break;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(labelExpr).append(" AS periodLabel, ")
           .append("       MIN(o.issuedDate) AS sortDate, ")
           .append("       SUM(o.totalAmount) AS totalRevenue, ")
           .append("       COUNT(*) AS billCount ")
           .append("FROM Orders o ")
           .append("WHERE o.paymentStatus = 'Paid' ");

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND o.issuedDate < DATEADD(day, 1, ?) ");
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

    public List<ProductSaleStat> getProductSalesChart(String periodType, java.sql.Date fromDate,
                                                        java.sql.Date toDate, String productId) {
        List<ProductSaleStat> list = new ArrayList<>();

        String groupExpr;
        String labelExpr;

        switch (periodType) {
            case "week":
                groupExpr = "DATEPART(year, o.issuedDate), DATEPART(week, o.issuedDate)";
                labelExpr = "CAST(DATEPART(year, o.issuedDate) AS VARCHAR) + '-W' + "
                          + "RIGHT('0' + CAST(DATEPART(week, o.issuedDate) AS VARCHAR), 2)";
                break;
            case "year":
                groupExpr = "DATEPART(year, o.issuedDate)";
                labelExpr = "CAST(DATEPART(year, o.issuedDate) AS VARCHAR)";
                break;
            case "day":
                groupExpr = "CONVERT(date, o.issuedDate)";
                labelExpr = "CONVERT(varchar(10), o.issuedDate, 23)";
                break;
            case "month":
            default:
                groupExpr = "FORMAT(o.issuedDate, 'yyyy-MM')";
                labelExpr = "FORMAT(o.issuedDate, 'yyyy-MM')";
                break;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(labelExpr).append(" AS periodLabel, ")
           .append("       MIN(o.issuedDate) AS sortDate, ")
           .append("       SUM(oi.quantity) AS quantitySold, ")
           .append("       SUM(CASE WHEN o.paymentStatus = 'Paid' THEN ")
           .append("            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS revenuePaid ")
           .append("FROM OrderItems oi ")
           .append("JOIN Orders o ON o.orderId = oi.orderId ")
           .append("JOIN ProductVariants pv ON pv.variantId = oi.variantId ")
           .append("JOIN Products p ON p.productId = pv.productId ")
           .append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND o.issuedDate < DATEADD(day, 1, ?) ");
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

    public List<ProductSalesRow> getProductSalesSummary(java.sql.Date fromDate, java.sql.Date toDate,
                                                          String productId) {
        List<ProductSalesRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.productId, p.name AS productName, "
              + "       SUM(oi.quantity) AS totalQuantity, "
              + "       SUM(CASE WHEN o.paymentStatus = 'Paid' THEN oi.quantity ELSE 0 END) AS paidQuantity, "
              + "       SUM(CASE WHEN o.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN oi.quantity ELSE 0 END) AS unpaidQuantity, "
              + "       SUM(CASE WHEN o.paymentStatus = 'Paid' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalRevenuePaid, "
              + "       SUM(CASE WHEN o.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalUnpaidAmount "
              + "FROM OrderItems oi "
              + "JOIN Orders o ON o.orderId = oi.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "WHERE o.orderStatus <> 'Cancelled' "
        );

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND o.issuedDate < DATEADD(day, 1, ?) ");
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

    public int[] getProductOrderCounts(java.sql.Date fromDate, java.sql.Date toDate, String productId) {
        int[] result = new int[]{0, 0};

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT CASE WHEN o.paymentStatus = 'Paid' THEN o.orderId END) AS paidCount, "
              + "       COUNT(DISTINCT CASE WHEN o.paymentStatus <> 'Paid' THEN o.orderId END) AS unpaidCount "
              + "FROM OrderItems oi "
              + "JOIN Orders o ON o.orderId = oi.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND o.issuedDate < DATEADD(day, 1, ?) ");
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
     * Bills no longer exist as a separate entity; this method now writes the
     * payment-related columns on the Orders row directly.
     */
    public boolean insertBill(Bill bill) {
        if (bill == null || isEmpty(bill.getBillId()) || isEmpty(bill.getOrderId())) {
            return false;
        }

        String sql = "UPDATE Orders SET "
                   + "paymentMethod = ?, paymentStatus = ?, issuedDate = COALESCE(issuedDate, GETDATE()) "
                   + "WHERE orderId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "COD");
            ps.setString(2, bill.getPaymentStatus() != null ? bill.getPaymentStatus() : "Pending");
            ps.setString(3, bill.getOrderId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("insertBill error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePaymentStatus(String billId, String paymentStatus) {
        if (isEmpty(billId) || isEmpty(paymentStatus)) {
            return false;
        }

        String sql = "UPDATE Orders SET paymentStatus = ? WHERE orderId = ?";

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

    public boolean updatePaymentStatusByOrderId(String orderId, String paymentStatus) {
        if (isEmpty(orderId) || isEmpty(paymentStatus)) {
            return false;
        }

        String sql = "UPDATE Orders SET paymentStatus = ? WHERE orderId = ?";

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
     * Legacy "bill exists?" helper. Bills no longer have their own table, so
     * every Order is implicitly a bill.
     */
    public boolean billExistsForOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM Orders WHERE orderId = ?";

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

    public Bill getBillByOrderId(String orderId) {
        if (isEmpty(orderId)) {
            return null;
        }

        String sql = "SELECT o.orderId AS billId, o.orderId, o.paymentMethod, o.paymentStatus, "
                   + "       o.issuedDate, o.totalAmount, "
                   + "       o.customerId, o.orderStatus, o.shippingAddress, o.placedAt, "
                   + "       c.fullName AS customerName, c.phone AS customerPhone "
                   + "FROM Orders o "
                   + "JOIN Customers c ON o.customerId = c.customerId "
                   + "WHERE o.orderId = ?";

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

    public List<ProductSalesRow> getProductSalesSummaryByFilter(String paidFilter, String productId, String sortBy) {
        List<ProductSalesRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.productId, p.name AS productName, "
              + "       SUM(oi.quantity) AS totalQuantity, "
              + "       SUM(CASE WHEN o.paymentStatus = 'Paid' THEN oi.quantity ELSE 0 END) AS paidQuantity, "
              + "       SUM(CASE WHEN o.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN oi.quantity ELSE 0 END) AS unpaidQuantity, "
              + "       SUM(CASE WHEN o.paymentStatus = 'Paid' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalRevenuePaid, "
              + "       SUM(CASE WHEN o.paymentStatus <> 'Paid' AND o.orderStatus <> 'Cancelled' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalUnpaidAmount "
              + "FROM OrderItems oi "
              + "JOIN Orders o ON o.orderId = oi.orderId "
              + "JOIN ProductVariants pv ON pv.variantId = oi.variantId "
              + "JOIN Products p ON p.productId = pv.productId "
              + "WHERE o.orderStatus <> 'Cancelled' "
        );

        List<Object> params = new ArrayList<>();

        if ("Paid".equals(paidFilter)) {
            sql.append("AND o.paymentStatus = 'Paid' ");
        } else if ("Unpaid".equals(paidFilter)) {
            sql.append("AND o.paymentStatus <> 'Paid' ");
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

    public List<TopProduct> getTopSellingProducts(int limit) {
        List<TopProduct> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) p.productId, p.name AS productName,
                   (SELECT TOP 1 pi.imageUrl FROM ProductImages pi
                    WHERE pi.productId = p.productId AND pi.isPrimary = 1) AS imageUrl,
                   SUM(oi.quantity) AS totalSold
            FROM OrderItems oi
            JOIN Orders o ON o.orderId = oi.orderId
            JOIN ProductVariants pv ON pv.variantId = oi.variantId
            JOIN Products p ON p.productId = pv.productId
            WHERE o.paymentStatus = 'Paid'
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

    public List<TopProduct> getTopRevenueProducts(int limit) {
        List<TopProduct> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) p.productId, p.name AS productName,
                   (SELECT TOP 1 pi.imageUrl FROM ProductImages pi
                    WHERE pi.productId = p.productId AND pi.isPrimary = 1) AS imageUrl,
                   SUM(oi.quantity) AS totalSold,
                   SUM(oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) AS totalRevenue
            FROM OrderItems oi
            JOIN Orders o ON o.orderId = oi.orderId
            JOIN ProductVariants pv ON pv.variantId = oi.variantId
            JOIN Products p ON p.productId = pv.productId
            WHERE o.paymentStatus = 'Paid'
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

    public List<CategorySales> getSalesByCategory() {
        List<CategorySales> list = new ArrayList<>();
        String sql = """
            SELECT c.categoryId, c.name AS categoryName,
                   SUM(oi.quantity) AS totalSold,
                   SUM(CASE WHEN o.paymentStatus = 'Paid'
                        THEN oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)
                        ELSE 0 END) AS totalRevenue
            FROM OrderItems oi
            JOIN Orders o ON o.orderId = oi.orderId
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

    public List<PaymentMethodStats> getSalesByPaymentMethod() {
        List<PaymentMethodStats> list = new ArrayList<>();
        String sql = """
            SELECT o.paymentMethod,
                   COUNT(*) AS totalBills,
                   SUM(o.totalAmount) AS totalRevenue
            FROM Orders o
            WHERE o.paymentStatus = 'Paid'
            GROUP BY o.paymentMethod
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