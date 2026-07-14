package Controllers;

import Models.Order;
import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CancelOrderServlet", urlPatterns = {"/customer/cancel-order"})
public class CancelOrderServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/customer/order-history");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Missing order ID.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        Order order = orderService.viewOrderDetailForCustomer(customerId, orderId);
        if (order == null) {
            session.setAttribute("errorMessage", "You cannot cancel this order.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        boolean cancelled = orderService.cancelOrder(orderId);
        session.setAttribute(cancelled ? "successMessage" : "errorMessage",
                cancelled ? "Order cancelled successfully." : "This order cannot be cancelled now.");

        response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + orderId);
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString();
        }

        Object user = session.getAttribute("USER");
        if (user == null) {
            return null;
        }

        String[] methodNames = {"getAccountId", "getCustomerId", "getUserId", "getId"};
        for (String methodName : methodNames) {
            try {
                Object value = user.getClass().getMethod(methodName).invoke(user);
                if (value != null && !value.toString().trim().isEmpty()) {
                    return value.toString();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
