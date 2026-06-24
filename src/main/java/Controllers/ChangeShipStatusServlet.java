package Controllers;

import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        response.setCharacterEncoding("UTF-8");

        String orderId = request.getParameter("orderId");
        String newStatus = request.getParameter("newStatus");

        if (isEmpty(newStatus)) {
            newStatus = request.getParameter("orderStatus");
        }

        if (isEmpty(orderId) || isEmpty(newStatus)) {
            response.sendRedirect(request.getContextPath() + "/staff/orders?error=missingShippingStatusData");
            return;
        }

        boolean result = orderService.changeShipStatus(orderId, newStatus);
        String message = result ? "shippingStatusSuccess" : "shippingStatusFailed";

        response.sendRedirect(request.getContextPath()
                + "/staff/order-detail?orderId=" + orderId.trim()
                + "&message=" + message);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
