/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DALs;

/**
 *
 * @author Admin
 */
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

        String sql = """
                     SELECT COUNT(*)
                     FROM Accounts
                     WHERE role='Customer'
                     """;

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
                     SELECT ISNULL(SUM(totalAmount),0)
                     FROM Bills
                     WHERE paymentStatus='Paid'
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
    public double getProfit() {

    return getRevenue() * 0.3;

    }
    

    public List<CustomerStatistic> getTopCustomers() {

        List<CustomerStatistic> list = new ArrayList<>();

        String sql = """
                     SELECT
                        A.accountId,
                        A.fullName,
                        COUNT(O.orderId) AS TotalOrders
                     FROM Accounts A
                     JOIN Orders O
                        ON A.accountId = O.customerId
                     WHERE A.role='Customer'
                     GROUP BY
                        A.accountId,
                        A.fullName
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

            OrderStatistic o =
                    new OrderStatistic();

            o.setStatus(
                    rs.getString("orderStatus"));

            o.setQuantity(
                    rs.getInt("Quantity"));

            list.add(o);
        }

    } catch (SQLException e) {
        System.out.println(
                "getOrderStatistics: "
                + e.getMessage());
    }

    return list;
}
    public List<CustomerStatistic>
searchCustomerByOrderQuantity(
        int quantity) {

    List<CustomerStatistic> list =
            new ArrayList<>();

    String sql = """
                 SELECT
                    A.accountId,
                    A.fullName,
                    COUNT(O.orderId)
                    AS TotalOrders
                 FROM Accounts A
                 JOIN Orders O
                    ON A.accountId =
                       O.customerId
                 WHERE A.role='Customer'
                 GROUP BY
                    A.accountId,
                    A.fullName
                 HAVING COUNT(O.orderId)
                    >= ?
                 ORDER BY TotalOrders DESC
                 """;

    try (PreparedStatement ps =
            connection.prepareStatement(sql)) {

        ps.setInt(1, quantity);

        ResultSet rs =
                ps.executeQuery();

        while (rs.next()) {

            CustomerStatistic c =
                    new CustomerStatistic();

            c.setAccountId(
                    rs.getString("accountId"));

            c.setFullName(
                    rs.getString("fullName"));

            c.setTotalOrders(
                    rs.getInt("TotalOrders"));

            list.add(c);
        }

    } catch (SQLException e) {
        System.out.println(
                "searchCustomerByOrderQuantity: "
                + e.getMessage());
    }

    return list;
}
}