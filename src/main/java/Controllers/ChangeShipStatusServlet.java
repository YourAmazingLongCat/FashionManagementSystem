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

        if (!isStaffOrAdmin(session)) {
            if (isAjax(request)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, false,
                        "You do not have permission to change order shipping status.");
            } else {
                response.sendRedirect(request.getContextPath() + "/auth/login");
            }
            return;
        }

        // Normal form posts use request parameters. X-* headers are kept as
        // an AJAX fallback so a malformed/multipart client request does not
        // lose the selected order/status information.
        String orderId = firstNonEmpty(
                request.getParameter("orderId"),
                request.getHeader("X-Order-Id"));
        String newStatus = firstNonEmpty(
                request.getParameter("newStatus"),
                request.getHeader("X-Order-Status"));

        if (isEmpty(orderId) || isEmpty(newStatus)) {
            session.setAttribute("errorMessage", "Missing order status information.");
            if (isAjax(request)) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Missing order status information.");
            } else {
                response.sendRedirect(request.getContextPath() + "/staff/orders");
            }
            return;
        }

        boolean updated = orderService.changeShipStatus(orderId, newStatus);
        String message = updated
                ? "Order status updated successfully."
                : "Order status was not changed. Shipping status must move forward in order: Confirmed -> Processing -> Shipping -> Delivered, and VNPay must be Paid.";
        session.setAttribute(updated ? "successMessage" : "errorMessage", message);

        if (isAjax(request)) {
            writeJson(response, HttpServletResponse.SC_OK, updated, message);
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
