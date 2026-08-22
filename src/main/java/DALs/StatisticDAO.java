package DALs;

import Models.ProductSale;
import Models.OrderStatistic;
import Models.CustomerStatistic;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatisticDAO extends DBContext {

    public StatisticDAO() {
        super();
    }

    /**
     * In the new schema, customers live in their own table, so this simply
     * counts rows from Customers.
     */
    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) FROM Customers";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("getTotalCustomers: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) FROM Orders";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("getTotalOrders: " + e.getMessage());
        }
        return 0;
    }

    public double getRevenue() {
        String sql = """
            SELECT ISNULL(SUM(OI.quantity * OI.unitPrice), 0)
            FROM OrderItems OI
            JOIN Orders O ON OI.orderId = O.orderId
            WHERE O.orderStatus <> 'Cancelled'
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("getRevenue: " + e.getMessage());
        }
        return 0;
    }

    public double getRevenue(String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT ISNULL(SUM(OI.quantity * OI.unitPrice), 0)
            FROM OrderItems OI
            JOIN Orders O ON OI.orderId = O.orderId
            WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("getRevenue (with date): " + e.getMessage());
        }
        return 0;
    }

    public double getCostOfGoodsSold() {
        return getCostOfGoodsSold(null, null);
    }

    public double getCostOfGoodsSold(String fromDate, String toDate) {
        // New schema: WarehouseImports has importedAt, not importDate.
        StringBuilder sql = new StringBuilder("""
            SELECT ISNULL(SUM(cost.totalCost), 0) AS totalCost
            FROM (
                SELECT
                    OI.variantId,
                    OI.quantity,
                    OI.orderId,
                    CASE
                        WHEN avgImport.avgPrice IS NOT NULL THEN avgImport.avgPrice * OI.quantity
                        ELSE P.basePrice * OI.quantity
                    END AS totalCost
                FROM OrderItems OI
                JOIN Orders O ON OI.orderId = O.orderId
                JOIN ProductVariants PV ON OI.variantId = PV.variantId
                JOIN Products P ON PV.productId = P.productId
                LEFT JOIN (
                    SELECT variantId, AVG(importPrice) AS avgPrice
                    FROM WarehouseImports
                    GROUP BY variantId
                ) avgImport ON OI.variantId = avgImport.variantId
                WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();

        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        sql.append(") cost");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("totalCost");
            }
        } catch (SQLException e) {
            System.out.println("getCostOfGoodsSold: " + e.getMessage());
        }
        return 0;
    }

    public double getProfit() {
        return getProfit(null, null);
    }

    public double getProfit(String fromDate, String toDate) {
        double revenue = getRevenue(fromDate, toDate);
        double cost = getCostOfGoodsSold(fromDate, toDate);
        return revenue - cost;
    }

    public List<CustomerStatistic> getTopCustomers() {
        List<CustomerStatistic> list = new ArrayList<>();
        String sql = """
                     SELECT
                        c.customerId AS accountId,
                        c.fullName,
                        COUNT(O.orderId) AS TotalOrders
                     FROM Customers c
                     JOIN Orders O ON c.customerId = O.customerId
                     WHERE O.orderStatus <> 'Cancelled'
                     GROUP BY c.customerId, c.fullName
                     ORDER BY TotalOrders DESC
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CustomerStatistic c = new CustomerStatistic();
                c.setAccountId(rs.getString("accountId"));
                c.setFullName(rs.getString("fullName"));
                c.setTotalOrders(rs.getInt("TotalOrders"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println("getTopCustomers: " + e.getMessage());
        }
        return list;
    }

    public List<OrderStatistic> getOrderStatistics() {
        List<OrderStatistic> list = new ArrayList<>();
        String sql = """
                     SELECT
                        orderStatus,
                        COUNT(*) AS Quantity
                     FROM Orders
                     GROUP BY orderStatus
                     ORDER BY orderStatus
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderStatistic o = new OrderStatistic();
                o.setStatus(rs.getString("orderStatus"));
                o.setQuantity(rs.getInt("Quantity"));
                list.add(o);
            }
        } catch (SQLException e) {
            System.out.println("getOrderStatistics: " + e.getMessage());
        }
        return list;
    }

    public List<CustomerStatistic> searchCustomerByOrderQuantity(int quantity) {
        List<CustomerStatistic> list = new ArrayList<>();
        String sql = """
                     SELECT
                        c.customerId AS accountId,
                        c.fullName,
                        COUNT(O.orderId) AS TotalOrders
                     FROM Customers c
                     JOIN Orders O ON c.customerId = O.customerId
                     WHERE O.orderStatus <> 'Cancelled'
                     GROUP BY c.customerId, c.fullName
                     HAVING COUNT(O.orderId) >= ?
                     ORDER BY TotalOrders DESC
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CustomerStatistic c = new CustomerStatistic();
                c.setAccountId(rs.getString("accountId"));
                c.setFullName(rs.getString("fullName"));
                c.setTotalOrders(rs.getInt("TotalOrders"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println("searchCustomerByOrderQuantity: " + e.getMessage());
        }
        return list;
    }

    public int getTotalProductSold(String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT ISNULL(SUM(OI.quantity), 0)
            FROM OrderItems OI
            JOIN Orders O ON OI.orderId = O.orderId
            WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("getTotalProductSold: " + e.getMessage());
        }
        return 0;
    }

    public List<ProductSale> getTopProducts(int limit, String fromDate, String toDate) {
        List<ProductSale> list = new ArrayList<>();
        StringBuilder sqlB = new StringBuilder("""
            SELECT TOP (?)
                P.productId,
                P.productId AS productCode,
                P.name AS productName,
                P.basePrice AS unitCost,
                AVG(OI.unitPrice) AS avgUnitPrice,
                SUM(OI.quantity) AS totalQty,
                SUM(OI.quantity * OI.unitPrice) AS revenue,
                SUM(CASE
                    WHEN avgImport.avgPrice IS NOT NULL THEN (OI.unitPrice - avgImport.avgPrice) * OI.quantity
                    ELSE (OI.unitPrice - P.basePrice) * OI.quantity
                END) AS profit
            FROM OrderItems OI
            JOIN ProductVariants PV ON OI.variantId = PV.variantId
            JOIN Products P ON PV.productId = P.productId
            JOIN Orders O ON OI.orderId = O.orderId
            LEFT JOIN (
                SELECT variantId, AVG(importPrice) AS avgPrice
                FROM WarehouseImports
                GROUP BY variantId
            ) avgImport ON OI.variantId = avgImport.variantId
            WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        params.add(limit);
        if (fromDate != null && !fromDate.isEmpty()) {
            sqlB.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sqlB.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        sqlB.append(" GROUP BY P.productId, P.name, P.basePrice");
        sqlB.append(" ORDER BY totalQty DESC, revenue DESC");

        try (PreparedStatement ps = connection.prepareStatement(sqlB.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductSale p = new ProductSale();
                p.setProductId(rs.getString("productId"));
                p.setProductCode(rs.getString("productCode"));
                p.setProductName(rs.getString("productName"));
                p.setUnitCost(rs.getDouble("unitCost"));
                p.setUnitPrice(rs.getDouble("avgUnitPrice"));
                p.setQuantitySold(rs.getInt("totalQty"));
                p.setRevenue(rs.getDouble("revenue"));
                p.setProfit(rs.getDouble("profit"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.out.println("getTopProducts: " + e.getMessage());
        }
        return list;
    }

    public List<ProductSale> getProductSales(String fromDate, String toDate) {
        List<ProductSale> list = new ArrayList<>();
        StringBuilder sqlB = new StringBuilder("""
            SELECT
                P.productId,
                P.productId AS productCode,
                P.name AS productName,
                P.basePrice AS unitCost,
                AVG(OI.unitPrice) AS avgUnitPrice,
                SUM(OI.quantity) AS totalQty,
                SUM(OI.quantity * OI.unitPrice) AS revenue,
                SUM(CASE
                    WHEN avgImport.avgPrice IS NOT NULL THEN (OI.unitPrice - avgImport.avgPrice) * OI.quantity
                    ELSE (OI.unitPrice - P.basePrice) * OI.quantity
                END) AS profit
            FROM OrderItems OI
            JOIN ProductVariants PV ON OI.variantId = PV.variantId
            JOIN Products P ON PV.productId = P.productId
            JOIN Orders O ON OI.orderId = O.orderId
            LEFT JOIN (
                SELECT variantId, AVG(importPrice) AS avgPrice
                FROM WarehouseImports
                GROUP BY variantId
            ) avgImport ON OI.variantId = avgImport.variantId
            WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        if (fromDate != null && !fromDate.isEmpty()) {
            sqlB.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sqlB.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        sqlB.append(" GROUP BY P.productId, P.name, P.basePrice");
        sqlB.append(" ORDER BY P.name");

        try (PreparedStatement ps = connection.prepareStatement(sqlB.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductSale p = new ProductSale();
                p.setProductId(rs.getString("productId"));
                p.setProductCode(rs.getString("productCode"));
                p.setProductName(rs.getString("productName"));
                p.setUnitCost(rs.getDouble("unitCost"));
                p.setUnitPrice(rs.getDouble("avgUnitPrice"));
                p.setQuantitySold(rs.getInt("totalQty"));
                p.setRevenue(rs.getDouble("revenue"));
                p.setProfit(rs.getDouble("profit"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.out.println("getProductSales: " + e.getMessage());
        }
        return list;
    }

    public List<CustomerStatistic> getTopSpenders(int limit, String fromDate, String toDate) {
        List<CustomerStatistic> list = new ArrayList<>();
        StringBuilder sqlB = new StringBuilder("""
            SELECT TOP (?)
                c.customerId AS accountId,
                c.fullName,
                COUNT(O.orderId) AS totalOrders,
                ISNULL(SUM(O.totalAmount), 0) AS totalSpent
            FROM Customers c
            JOIN Orders O ON c.customerId = O.customerId
            WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        params.add(limit);
        if (fromDate != null && !fromDate.isEmpty()) {
            sqlB.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sqlB.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        sqlB.append(" GROUP BY c.customerId, c.fullName");
        sqlB.append(" ORDER BY totalSpent DESC, totalOrders DESC");

        try (PreparedStatement ps = connection.prepareStatement(sqlB.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CustomerStatistic c = new CustomerStatistic();
                c.setAccountId(rs.getString("accountId"));
                c.setFullName(rs.getString("fullName"));
                c.setTotalOrders(rs.getInt("totalOrders"));
                c.setTotalSpent(rs.getDouble("totalSpent"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println("getTopSpenders: " + e.getMessage());
        }
        return list;
    }

    /**
     * New schema: WarehouseImports.importedAt replaces importDate.
     */
    public double getTotalImportCost(String fromDate, String toDate) {
        StringBuilder sqlB = new StringBuilder("""
            SELECT ISNULL(SUM(quantity * importPrice), 0)
            FROM WarehouseImports wi
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (fromDate != null && !fromDate.isEmpty()) {
            sqlB.append(" AND wi.importedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sqlB.append(" AND wi.importedAt <= ?");
            params.add(toDate + " 23:59:59");
        }

        try (PreparedStatement ps = connection.prepareStatement(sqlB.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("getTotalImportCost: " + e.getMessage());
        }
        return 0;
    }
}