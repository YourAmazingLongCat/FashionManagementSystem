/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) FROM Accounts WHERE role='Customer'";
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

    // Revenue: sum of Orders.totalAmount (Bills table is empty, use Orders instead)
    public double getRevenue() {
        String sql = "SELECT ISNULL(SUM(totalAmount), 0) FROM Orders WHERE orderStatus <> 'Cancelled'";
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

    public double getProfit() {
        String sql = """
                     SELECT ISNULL(
                         SUM((OI.unitPrice - P.basePrice) * OI.quantity),0)
                     FROM OrderItems OI
                     JOIN ProductVariants PV ON OI.variantId = PV.variantId
                     JOIN Products P ON PV.productId = P.productId
                     JOIN Orders O ON OI.orderId = O.orderId
                     WHERE O.orderStatus <> 'Cancelled'
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("getProfit: " + e.getMessage());
        }
        return 0;
    }

    public List<CustomerStatistic> getTopCustomers() {
        List<CustomerStatistic> list = new ArrayList<>();
        String sql = """
                     SELECT
                        A.accountId,
                        A.fullName,
                        COUNT(O.orderId) AS TotalOrders
                     FROM Accounts A
                     JOIN Orders O ON A.accountId = O.customerId
                     WHERE A.role='Customer'
                       AND O.orderStatus <> 'Cancelled'
                     GROUP BY A.accountId, A.fullName
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
                        A.accountId,
                        A.fullName,
                        COUNT(O.orderId) AS TotalOrders
                     FROM Accounts A
                     JOIN Orders O ON A.accountId = O.customerId
                     WHERE A.role='Customer'
                       AND O.orderStatus <> 'Cancelled'
                     GROUP BY A.accountId, A.fullName
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

    // Revenue by date range: use Orders.totalAmount (not Bills, which is empty)
    public double getRevenue(String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT ISNULL(SUM(totalAmount), 0)
            FROM Orders
            WHERE orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND placedAt <= ?");
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

    // Profit by date range
    public double getProfit(String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT ISNULL(SUM((OI.unitPrice - P.basePrice) * OI.quantity), 0)
            FROM OrderItems OI
            JOIN ProductVariants PV ON OI.variantId = PV.variantId
            JOIN Products P ON PV.productId = P.productId
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
            System.out.println("getProfit (with date): " + e.getMessage());
        }
        return 0;
    }

    // Cost of Goods Sold by date range
    public double getCostOfGoodsSold(String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT ISNULL(SUM(P.basePrice * OI.quantity), 0)
            FROM OrderItems OI
            JOIN ProductVariants PV ON OI.variantId = PV.variantId
            JOIN Products P ON PV.productId = P.productId
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
            System.out.println("getCostOfGoodsSold: " + e.getMessage());
        }
        return 0;
    }

    // Total products sold by date range
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

    // Top selling products (limited)
    public List<ProductSale> getTopProducts(int limit, String fromDate, String toDate) {
        List<ProductSale> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT TOP (?)
                P.productId,
                P.productId AS productCode,
                P.name AS productName,
                MIN(P.basePrice) AS unitCost,
                AVG(OI.unitPrice) AS avgUnitPrice,
                SUM(OI.quantity) AS totalQty,
                SUM(OI.unitPrice * OI.quantity) AS revenue,
                SUM((OI.unitPrice - P.basePrice) * OI.quantity) AS profit
            FROM OrderItems OI
            JOIN ProductVariants PV ON OI.variantId = PV.variantId
            JOIN Products P ON PV.productId = P.productId
            JOIN Orders O ON OI.orderId = O.orderId
            WHERE O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        params.add(limit);
        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        sql.append(" GROUP BY P.productId, P.name, P.basePrice");
        sql.append(" ORDER BY totalQty DESC, revenue DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
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

    // All product sales (no limit)
    public List<ProductSale> getProductSales(String fromDate, String toDate) {
        List<ProductSale> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT
                P.productId,
                P.productId AS productCode,
                P.name AS productName,
                MIN(P.basePrice) AS unitCost,
                AVG(OI.unitPrice) AS avgUnitPrice,
                SUM(OI.quantity) AS totalQty,
                SUM(OI.unitPrice * OI.quantity) AS revenue,
                SUM((OI.unitPrice - P.basePrice) * OI.quantity) AS profit
            FROM OrderItems OI
            JOIN ProductVariants PV ON OI.variantId = PV.variantId
            JOIN Products P ON PV.productId = P.productId
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
        sql.append(" GROUP BY P.productId, P.name, P.basePrice");
        sql.append(" ORDER BY P.name");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
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

    // Top spenders: compute spending from Orders.totalAmount (not Bills, which is empty)
    public List<CustomerStatistic> getTopSpenders(int limit, String fromDate, String toDate) {
        List<CustomerStatistic> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT TOP (?)
                A.accountId,
                A.fullName,
                COUNT(O.orderId) AS totalOrders,
                ISNULL(SUM(O.totalAmount), 0) AS totalSpent
            FROM Accounts A
            JOIN Orders O ON A.accountId = O.customerId
            WHERE A.role = 'Customer'
              AND O.orderStatus <> 'Cancelled'
        """);
        List<Object> params = new ArrayList<>();
        params.add(limit);
        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND O.placedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND O.placedAt <= ?");
            params.add(toDate + " 23:59:59");
        }
        sql.append(" GROUP BY A.accountId, A.fullName");
        sql.append(" ORDER BY totalSpent DESC, totalOrders DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
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
}
