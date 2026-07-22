package DALs;

import Models.Bill;
import Models.BillOrderItem;
import Models.ProductOption;
import Models.ProductSaleStat;
import Models.ProductSalesRow;
import Models.RevenueStat;
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
 * DAL cho chức năng Bill Management.
 *
 * GHI CHÚ:
 * - DBContext.getConnection() là method INSTANCE (không static), nên mỗi
 *   lần truy vấn phải tạo mới new DBContext() để mở 1 connection riêng,
 *   rồi để try-with-resources tự đóng connection đó sau khi dùng xong.
 * - Doanh thu (revenue) chỉ tính trên các Bill có paymentStatus = 'Paid',
 *   vì Bill 'Pending'/'Failed' chưa thực sự thu được tiền.
 */
public class BillDAO {

    // ================= VIEW / SEARCH / FILTER BILL =================

    /**
     * Lấy toàn bộ hóa đơn (không filter), mới nhất trước.
     */
    public List<Bill> getAllBills() {
        return searchBills(null, null, null, null, null);
    }

    /**
     * Tìm kiếm + lọc hóa đơn.
     *
     * @param keyword       tìm theo billId, orderId, tên khách hàng, sđt (có thể null/rỗng)
     * @param paymentStatus lọc theo trạng thái thanh toán (có thể null = tất cả)
     * @param orderStatus   lọc theo trạng thái đơn hàng (có thể null = tất cả)
     * @param fromDate      lọc issuedDate >= fromDate (có thể null)
     * @param toDate        lọc issuedDate <= toDate 23:59:59 (có thể null)
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
            // cộng thêm gần 1 ngày để bao trọn ngày toDate
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
     * Đếm tổng số bills thỏa điều kiện filter.
     */
    public int countBills(String keyword, String paymentStatus, String orderStatus,
                          java.sql.Date fromDate, java.sql.Date toDate) {
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

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }

        if (toDate != null) {
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
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
     * Tìm kiếm + lọc hóa đơn có phân trang.
     */
    public List<Bill> searchBillsPaginated(String keyword, String paymentStatus, String orderStatus,
                                            java.sql.Date fromDate, java.sql.Date toDate,
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

        if (fromDate != null) {
            sql.append("AND b.issuedDate >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }

        if (toDate != null) {
            sql.append("AND b.issuedDate < DATEADD(day, 1, ?) ");
            params.add(new Timestamp(toDate.getTime()));
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
     * Lấy 1 hóa đơn theo billId, kèm thông tin khách hàng / đơn hàng.
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
     * Lấy danh sách sản phẩm (chi tiết) thuộc 1 hóa đơn, dựa trên orderId
     * liên kết với hóa đơn đó.
     */
    public List<BillOrderItem> getBillDetails(String billId) {
        return searchBillDetails(billId, null);
    }

    /**
     * Tìm kiếm chi tiết hóa đơn theo tên sản phẩm / sku, trong phạm vi 1 hóa đơn.
     *
     * @param billId  hóa đơn cần xem chi tiết
     * @param keyword tên sản phẩm hoặc sku (có thể null/rỗng = lấy hết)
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

    // ================= REVENUE CHART (biểu đồ doanh thu) =================

    /**
     * Thống kê tổng doanh thu theo mốc thời gian (ngày/tuần/tháng/năm)
     * để vẽ biểu đồ đường (line chart). Chỉ tính các bill paymentStatus = 'Paid'.
     *
     * @param periodType "day" | "week" | "month" | "year" (đã validate ở Servlet)
     * @param fromDate   có thể null
     * @param toDate     có thể null
     */
    public List<RevenueStat> getRevenueStats(String periodType, java.sql.Date fromDate, java.sql.Date toDate) {
        List<RevenueStat> list = new ArrayList<>();

        // groupExpr/labelExpr được chọn từ danh sách cố định (whitelist),
        // KHÔNG lấy trực tiếp từ input người dùng -> tránh SQL Injection.
        String groupExpr;
        String labelExpr;

        switch (periodType) {
            case "week":
                // Gom theo năm + số tuần trong năm
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

    // ================= PRODUCT SALES (thống kê theo sản phẩm) =================

    /**
     * Lấy danh sách sản phẩm (id + tên) để đổ vào dropdown lọc theo sản phẩm
     * ở màn hình thống kê "Theo sản phẩm".
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
     * Thống kê số lượng bán ra + doanh thu đã thu (Paid) theo mốc thời gian
     * (ngày/tuần/tháng/năm), dùng cho biểu đồ "Số lượng hàng bán được".
     * Có thể lọc theo 1 sản phẩm cụ thể (productId) hoặc null/rỗng = tất cả
     * sản phẩm. Số lượng bán ra được tính trên TẤT CẢ đơn hàng đã lập hóa
     * đơn (không phân biệt trạng thái thanh toán), còn doanh thu chỉ tính
     * phần đã thu (Paid).
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
     * Danh sách chi tiết theo từng sản phẩm trong khoảng thời gian đang lọc:
     * tổng số lượng bán ra, tổng tiền đã thu (Paid), tổng tiền còn thiếu
     * (các bill có paymentStatus khác 'Paid'). Có thể lọc theo 1 sản phẩm cụ
     * thể hoặc null/rỗng = tất cả sản phẩm.
     */
    public List<ProductSalesRow> getProductSalesSummary(java.sql.Date fromDate, java.sql.Date toDate,
                                                          String productId) {
        List<ProductSalesRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.productId, p.name AS productName, "
              + "       SUM(oi.quantity) AS totalQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN oi.quantity ELSE 0 END) AS paidQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus <> 'Paid' THEN oi.quantity ELSE 0 END) AS unpaidQuantity, "
              + "       SUM(CASE WHEN b.paymentStatus = 'Paid' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalRevenuePaid, "
              + "       SUM(CASE WHEN b.paymentStatus <> 'Paid' THEN "
              + "            (oi.quantity * oi.unitPrice - ISNULL(oi.discountAmount, 0)) ELSE 0 END) AS totalUnpaidAmount "
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
     * Đếm số lượng hóa đơn (bill) đã thanh toán (Paid) và chưa thanh toán
     * (khác Paid) có chứa ít nhất 1 sản phẩm khớp bộ lọc hiện tại (productId
     * null/rỗng = tất cả sản phẩm), trong khoảng thời gian đang lọc.
     * Đếm DISTINCT theo billId vì 1 bill có thể có nhiều dòng OrderItems.
     *
     * @return mảng 2 phần tử: [0] = số đơn đã thanh toán, [1] = số đơn chưa thanh toán
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

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}