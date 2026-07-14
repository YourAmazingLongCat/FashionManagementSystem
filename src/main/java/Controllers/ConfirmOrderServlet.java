package Controllers;

import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ConfirmOrderServlet", urlPatterns = {"/staff/confirm-order"})
public class ConfirmOrderServlet extends HttpServlet {

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

        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Missing order ID.");
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }

        boolean confirmed = orderService.confirmOrder(orderId);
        session.setAttribute(confirmed ? "successMessage" : "errorMessage",
                confirmed ? "Order confirmed successfully." : "This order cannot be confirmed.");

        response.sendRedirect(request.getContextPath() + "/staff/order-detail?orderId=" + orderId);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
