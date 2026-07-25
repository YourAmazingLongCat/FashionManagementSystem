package Controllers;

import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ChangeShipStatusServlet", urlPatterns = {"/staff/change-shipping-status"})
public class ChangeShipStatusServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/staff/orders");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String orderId = trim(request.getParameter("orderId"));
        String newStatus = trim(request.getParameter("newStatus"));

        if (isEmpty(orderId) || isEmpty(newStatus)) {
            session.setAttribute("errorMessage", "Missing order status information.");
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }

        boolean updated = orderService.changeShipStatus(orderId, newStatus);
        session.setAttribute(updated ? "successMessage" : "errorMessage",
                updated
                        ? "Order status updated successfully."
                        : "Order status was not changed. The customer may not have pressed Place order yet, the transition may be invalid, or Wallet/VNPay payment may not be Paid.");

        response.sendRedirect(request.getContextPath() + "/staff/order-detail?orderId=" + orderId);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
