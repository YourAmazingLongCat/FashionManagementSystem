package Controllers;

import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        response.setCharacterEncoding("UTF-8");

        String orderId = request.getParameter("orderId");

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/staff/orders?error=missingOrderId");
            return;
        }

        boolean result = orderService.confirmOrder(orderId);
        String message = result ? "confirmSuccess" : "confirmFailed";

        response.sendRedirect(request.getContextPath()
                + "/staff/order-detail?orderId=" + orderId.trim()
                + "&message=" + message);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
