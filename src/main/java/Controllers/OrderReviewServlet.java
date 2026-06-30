package Controllers;

import Models.Order;
import Models.OrderItem;
import Models.Account;
import DALs.OrderDAO;
import DALs.OrderItemDAO;

import java.io.IOException;
import java.math.BigDecimal;
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

        Account user = (Account) session.getAttribute("user");

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
        Order order = orderDAO.getOrderById(orderId);

        if (order == null) {
            request.setAttribute("error", "Order not found!");
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
            return;
        }

        // Use accountId instead of userId
        if (!order.getCustomerId().equals(user.getAccountId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return;
        }

        // Use OrderItemDAO instead of OrderDAO
        OrderItemDAO orderItemDAO = new OrderItemDAO();
        List<OrderItem> orderItems = orderItemDAO.getOrderItemsByOrderId(orderId);

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                // Use getSubTotal() which returns BigDecimal
                totalAmount = totalAmount.add(item.getSubTotal());
            }
        }

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("totalAmount", totalAmount);

        request.getRequestDispatcher("/views/orderReview.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }
}
