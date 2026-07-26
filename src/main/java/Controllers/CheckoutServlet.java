package Controllers;

import Models.Account;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Compatibility route for old links that still point to /customer/checkout.
 *
 * The project now uses one customer Order page for both cases:
 * - immediately after the Cart Checkout button creates a Pending order;
 * - when the customer returns later from Order History.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = {"/customer/checkout"})
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        redirectToOrderPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        redirectToOrderPage(request, response);
    }

    private void redirectToOrderPage(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (getCustomerId(session) == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        if (isEmpty(orderId)) {
            Object pendingOrderId = session.getAttribute("pendingCheckoutOrderId");
            if (pendingOrderId != null) {
                orderId = trim(pendingOrderId.toString());
            }
        }

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId);
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString();
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
