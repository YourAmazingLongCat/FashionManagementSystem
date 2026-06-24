package Controllers;

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
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/Pages/Authentication/login.jsp");
            return;
        }

        String orderId = request.getParameter("orderId");

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history?error=missingOrderId");
            return;
        }

        boolean result = orderService.cancelOrder(orderId, customerId);
        String message = result ? "cancelSuccess" : "cancelFailed";

        response.sendRedirect(request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId.trim()
                + "&message=" + message);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
