/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.OrderDAO;
import DALs.WarehouseDAO;
import Models.Order;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author CE181629 - Ngo Manh Quan
 */
public class OrderManagementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action") != null
                ? request.getParameter("action")
                : "";

        switch (action) {
            case "add":
                doGetAdd(request, response);
                break;
            case "update":
                doGetUpdate(request, response);
                break;
            case "delete":
                doGetDelete(request, response);
                break;
            default:
                doGetRead(request, response);
                break;
        }
    }

    private void doGetRead(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        OrderDAO orderDAO = new OrderDAO();
        List<Order> orders = orderDAO.GetAllOrders();

        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/views/orderManagement/listOrder.jsp").forward(request, response);
    }

    private void doGetAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.getRequestDispatcher("/views/orderManagement/addOrder.jsp").forward(request, response);
    }

    private void doGetUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int orderId = Integer.parseInt(request.getParameter("id"));
        OrderDAO orderDAO = new OrderDAO();
        Order order = orderDAO.GetOrderById(orderId);

        request.setAttribute("order", order);
        request.getRequestDispatcher("/views/orderManagement/updateOrder.jsp").forward(request, response);
    }

    private void doGetDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int orderId = Integer.parseInt(request.getParameter("id"));
        OrderDAO orderDAO = new OrderDAO();
        Order order = orderDAO.GetOrderById(orderId);

        request.setAttribute("order", order);
        request.getRequestDispatcher("/views/orderManagement/deleteOrder.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action") != null
                ? request.getParameter("action")
                : "";

        switch (action) {
            case "add":
                doPostAdd(request, response);
                break;
            case "update":
                doPostUpdate(request, response);
                break;
            case "delete":
                doPostDelete(request, response);
                break;
        }
    }

    private void doPostAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int userId = Integer.parseInt(request.getParameter("userId"));
        String status = request.getParameter("status");
        double totalAmount = Double.parseDouble(request.getParameter("totalAmount"));

        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.CreateOrder(userId, LocalDateTime.now().toString(), status, totalAmount);

        if (orderId >= 0) {
            response.sendRedirect(request.getContextPath() + "/order");
        } else {
            request.setAttribute("error", "Add order error!");
            request.getRequestDispatcher("/views/orderManagement/addOrder.jsp").forward(request, response);
        }
    }

    private void doPostUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int orderId = Integer.parseInt(request.getParameter("id"));
        int userId = Integer.parseInt(request.getParameter("userId"));
        String status = request.getParameter("status");
        int totalAmount = Integer.parseInt(request.getParameter("totalAmount"));
        String orderDate = request.getParameter("orderDate");
        // ⚠️ cần format yyyy-MM-ddTHH:mm nếu dùng <input type="datetime-local">

        OrderDAO orderDAO = new OrderDAO();
        boolean rs = orderDAO.UpdateOrderById(orderId, userId, orderDate, status, totalAmount);

        if (rs) {
            // Auto-export stock when order is completed
            if ("Completed".equalsIgnoreCase(status)) {
                try {
                    WarehouseDAO warehouseDAO = new WarehouseDAO();
                    String orderIdStr = "ORD" + String.format("%05d", orderId);
                    warehouseDAO.deductStockForOrder(orderIdStr);
                    System.out.println("Auto-export stock for order: " + orderIdStr);
                } catch (Exception ex) {
                    System.out.println("Auto-export stock error: " + ex.getMessage());
                }
            }
            response.sendRedirect(request.getContextPath() + "/order");
        } else {
            request.setAttribute("error", "Update order error!");
            request.getRequestDispatcher("/views/orderManagement/updateOrder.jsp").forward(request, response);
        }
    }

    private void doPostDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int orderId = Integer.parseInt(request.getParameter("id"));

        OrderDAO orderDAO = new OrderDAO();
        boolean rs = orderDAO.DeleteOrderById(orderId);

        if (rs) {
            response.sendRedirect(request.getContextPath() + "/order");
        } else {
            request.setAttribute("error", "Delete order error!");
            request.getRequestDispatcher("/views/orderManagement/deleteOrder.jsp").forward(request, response);
        }
    }

}
