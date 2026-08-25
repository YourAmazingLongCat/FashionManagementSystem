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
 * Compatibility endpoint kept for old bookmarks. Payment management is now
 * part of Order management, so staff are redirected to Manage Orders.
 */
@WebServlet(name = "StaffPaymentServlet", urlPatterns = {"/staff/payments"})
public class StaffPaymentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");

        if (user == null || !isStaffOrAdmin(user)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/staff/orders");
    }

    private boolean isStaffOrAdmin(Account user) {
        return "Staff".equalsIgnoreCase(user.getRole())
                || "Admin".equalsIgnoreCase(user.getRole());
    }
}
