package Controllers;

import Models.Account;
import Models.Order;
import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CustomerSearchOrderDetailServlet", urlPatterns = {"/customer/search-order-detail"})
public class CustomerSearchOrderDetailServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        if (isEmpty(orderId)) {
            orderId = trim(request.getParameter("keyword"));
        }

        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Please enter an order ID.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        Order order = orderService.searchOrderDetailForCustomer(customerId, orderId);
        if (order == null) {
            session.setAttribute("errorMessage", "Order not found.");
            response.sendRedirect(request.getContextPath()
                    + "/customer/order-history?keyword="
                    + java.net.URLEncoder.encode(orderId, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/customer/order-detail?orderId=" + order.getOrderId());
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString().trim();
        }

        Object user = session.getAttribute("USER");
        if (user instanceof Account) {
            return ((Account) user).getAccountId();
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
