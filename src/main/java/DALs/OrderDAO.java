/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DALs;


import Utils.DBContext;
import Models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author CE181629 - Ngo Manh Quan
 */
public class OrderDAO extends DBContext {

    public OrderDAO() {
        super();
    }

    public List<Order> GetAllOrders() {
        List<Order> listOrders = new ArrayList<>();
        String query = "SELECT * FROM Orders";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("OrderID"),
                        rs.getInt("UserID"),
                        rs.getString("OrderDate"),
                        rs.getString("Status"),
                        rs.getInt("TotalAmount")
                );
                listOrders.add(order);
            }
        } catch (SQLException e) {
            System.out.println("getAllOrders error: " + e);
        }
        return listOrders;
    }

    public Order GetOrderById(int id) {
        String query = "SELECT * FROM Orders WHERE OrderID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Order(
                        rs.getInt("OrderID"),
                        rs.getInt("UserID"),
                        rs.getString("OrderDate"),
                        rs.getString("Status"),
                        rs.getInt("TotalAmount")
                );
            }
        } catch (SQLException e) {
            System.out.println("getOrderById error: " + e);
        }
        return null;
    }

    public int CreateOrder(int userId, String orderDate, String status, double totalAmount) {
        String query = "INSERT INTO Orders (UserID, OrderDate, Status, TotalAmount) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setString(2, orderDate);
            ps.setString(3, status);
            ps.setDouble(4, totalAmount);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            
            if (rs.next()) {
                return rs.getInt(1); 
            }
            
        } catch (SQLException e) {
            System.out.println("createOrder error: " + e);
        }
        return -1;
    }

    public boolean UpdateOrderById(int id, int userId, String orderDate, String status, double totalAmount) {
        String query = "UPDATE Orders SET UserID = ?, OrderDate = ?, Status = ?, TotalAmount = ? WHERE OrderID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setString(2, orderDate);
            ps.setString(3, status);
            ps.setDouble(4, totalAmount);
            ps.setInt(5, id);
            int rs = ps.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            System.out.println("updateOrderById error: " + e);
        }
        return false;
    }

    public boolean DeleteOrderById(int id) {
        String query = "DELETE FROM Orders WHERE OrderID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            int rs = ps.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            System.out.println("deleteOrderById error: " + e);
        }
        return false;
    }
    
//    public void AddOrderItems(int orderId, List<Cart> cart){
//        String query = "INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
//        try {
//            PreparedStatement stmt = connection.prepareStatement(query);
//            
//            for (Cart item : cart) {
//                stmt.setInt(1, orderId);
//                stmt.setInt(2, item.getProductID());
//                stmt.setInt(3, item.getQuantity());
//                stmt.setDouble(4, item.getProductPrice());
//                stmt.addBatch();
//            }
//            
//            stmt.executeBatch();
//        } catch (SQLException e) {
//            System.out.println("deleteOrderById error: " + e);
//        }
//    }
}