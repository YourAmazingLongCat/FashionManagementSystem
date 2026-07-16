package Controllers;

import Models.CartItem;
import Models.Order;
import Services.OrderService;
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

@WebServlet(name = "OrderReviewServlet", urlPatterns = {"/customer/order-review"})
public class OrderReviewServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

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
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();

        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            request.setAttribute("errorMessage", "Your cart is empty.");
            request.getRequestDispatcher("/Pages/Customer/Cart.jsp").forward(request, response);
            return;
        }

        String shippingAddress = request.getParameter("shippingAddress");
        String phone = request.getParameter("phone");

        if (shippingAddress == null || shippingAddress.trim().isEmpty()
                || phone == null || phone.trim().isEmpty()) {

            request.setAttribute("errorMessage", "Please enter shipping address and phone number.");
            request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
            return;
        }

        Order orderPreview = orderService.reviewOrder(
                customerId,
                shippingAddress.trim(),
                phone.trim(),
                cart
        );

        if (orderPreview == null) {
            request.setAttribute("errorMessage", "Cannot review order. Please check your cart.");
            request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
            return;
        }

        request.setAttribute("orderPreview", orderPreview);
        request.setAttribute("cart", cart);

        request.getRequestDispatcher("/Pages/Customer/orderReview.jsp").forward(request, response);
    }
}
