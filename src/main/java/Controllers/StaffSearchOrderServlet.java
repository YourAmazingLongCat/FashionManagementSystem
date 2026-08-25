package Controllers;

import Models.Account;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Legacy order-search endpoint. Search is handled by the Manage Orders
 * section, so this route forwards the keyword to /staff/orders.
 */
@WebServlet(name = "StaffSearchOrderServlet", urlPatterns = {"/staff/search-orders"})
public class StaffSearchOrderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account user = session == null ? null : (Account) session.getAttribute("USER");
        if (user == null || !isStaffOrAdmin(user)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String keyword = request.getParameter("keyword");
        String target = request.getContextPath() + "/staff/orders";
        if (keyword != null && !keyword.trim().isEmpty()) {
            target += "?keyword=" + URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        }
        response.sendRedirect(target);
    }

    private boolean isStaffOrAdmin(Account user) {
        return "Staff".equalsIgnoreCase(user.getRole())
                || "Admin".equalsIgnoreCase(user.getRole());
    }
}
