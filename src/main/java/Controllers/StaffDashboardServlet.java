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
 * Legacy Staff Dashboard route.
 * The standalone Staff dashboard is no longer rendered. Staff now enter the
 * management workspace through Manage Products, where Manage Orders is a
 * sibling section in the same sidebar.
 */
@WebServlet(name = "StaffDashboardServlet", urlPatterns = {"/staff/dashboard"})
public class StaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account currentUser = session == null ? null : (Account) session.getAttribute("USER");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String role = currentUser.getRole();
        if (role != null && role.equalsIgnoreCase("Staff")) {
            response.sendRedirect(request.getContextPath() + "/staff/products");
        } else if (role != null && role.equalsIgnoreCase("Admin")) {
            response.sendRedirect(request.getContextPath() + "/Admin");
        } else {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}
