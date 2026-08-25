package Controllers;

import Models.Account;
import Services.OrderService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StaffCancelOrderServlet", urlPatterns = {"/staff/cancel-order"})
public class StaffCancelOrderServlet extends HttpServlet {

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

        if (!isStaffOrAdmin(session)) {
            if (isAjax(request)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, false,
                        "You do not have permission to cancel orders.");
            } else {
                response.sendRedirect(request.getContextPath() + "/auth/login");
            }
            return;
        }

        String orderId = firstNonEmpty(
                request.getParameter("orderId"),
                request.getHeader("X-Order-Id"));

        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Missing order ID.");
            if (isAjax(request)) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Missing order ID.");
            } else {
                response.sendRedirect(request.getContextPath() + "/staff/orders");
            }
            return;
        }

        boolean cancelled = orderService.cancelOrder(orderId);
        String message = cancelled
                ? "Order cancelled successfully. Payment was refunded or cancelled when applicable."
                : "This order cannot be cancelled before the customer presses Place order or once it reaches Shipping.";
        session.setAttribute(cancelled ? "successMessage" : "errorMessage", message);

        if (isAjax(request)) {
            writeJson(response, HttpServletResponse.SC_OK, cancelled, message);
            return;
        }

        redirectToPopup(request, response, orderId);
    }

    private void redirectToPopup(HttpServletRequest request, HttpServletResponse response, String orderId)
            throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/staff/orders?openOrder="
                + URLEncoder.encode(orderId, StandardCharsets.UTF_8));
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private boolean isStaffOrAdmin(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object userObject = session.getAttribute("USER");
        if (!(userObject instanceof Account)) {
            return false;
        }
        Account user = (Account) userObject;
        return "Staff".equalsIgnoreCase(user.getRole())
                || "Admin".equalsIgnoreCase(user.getRole());
    }

    private void writeJson(HttpServletResponse response, int status, boolean success, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":" + success + ",\"message\":\"" + jsonEscape(message) + "\"}");
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String firstNonEmpty(String first, String second) {
        String value = trim(first);
        if (!isEmpty(value)) {
            return value;
        }
        return trim(second);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
