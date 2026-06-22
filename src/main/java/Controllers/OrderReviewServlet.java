/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import Models.Order;
import Models.OrderItem;
import Models.User;
import DALs.OrderDAO;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author CE181629 - Ngo Manh Quan
 */
@WebServlet(name = "OrderReviewServlet", urlPatterns = {"/OrderReviewServlet"})
public class OrderReviewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action") != null
                ? request.getParameter("action")
                : "";

        switch (action) {
            default:
                doGetReview(request, response);
                break;
        }
    }

    private void doGetReview(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String orderId = request.getParameter("orderId");

        if (orderId == null || orderId.trim().isEmpty()) {
            request.setAttribute("error", "Invalid order ID!");
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        Order order = orderDAO.GetOrderById(orderId);

        if (order == null) {
            request.setAttribute("error", "Order not found!");
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
            return;
        }

        if (!order.getCustomerId().equals(user.getUserId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return;
        }

        List<OrderItem> orderItems = orderDAO.GetOrderItems(orderId);

        double totalAmount = 0;

        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                totalAmount += item.getUnitPrice() * item.getQuantity();
            }
        }

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("totalAmount", totalAmount);

        request.getRequestDispatcher("/views/orderReview.jsp").forward(request, response);
    }

    private double calculateTotal(List<OrderItem> orderItems) {

        double total = 0;

        if (orderItems == null) {
            return total;
        }

        for (OrderItem item : orderItems) {
            total += item.getUnitPrice() * item.getQuantity();
        }

        return total;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }
}
