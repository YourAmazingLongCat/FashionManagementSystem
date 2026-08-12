package Controllers;

import java.io.IOException;

import DALs.AccountDAO;
import Models.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for Login logic (Fashion Store).
 */
@WebServlet(name = "LoginControllers", urlPatterns = {"/auth/login"})
public class LoginControllers extends HttpServlet {

    /**
     * Handle GET: show login page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to Login.jsp
        request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
    }

    /**
     * Handle POST: process login form submit.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Read form data (matches name="email" and name="password" in JSP)
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 2. Call DAO to check DB
        AccountDAO dao = new AccountDAO();
        Account acc = dao.checkLogin(email, password);

        // 3. Handle result
        if (acc != null) {

            // If status is not null and NOT "Active" (case-insensitive)
            if (acc.getStatus() != null && !acc.getStatus().equalsIgnoreCase("Active")) {
                request.setAttribute("errorMessage", "Your account is locked or not activated!");
                request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
                return;
            }

            // Login success: store Account in session
            HttpSession session = request.getSession();
            session.setAttribute("USER", acc);

            // Redirect by role: Staff -> Product Mgmt, Admin -> Dashboard, Customer -> Home
            String role = acc.getRole();
            if (role != null && role.equalsIgnoreCase("Staff")) {
                response.sendRedirect(request.getContextPath() + "/staff/products");
            } else if (role != null && role.equalsIgnoreCase("Admin")) {
                response.sendRedirect(request.getContextPath() + "/Admin");
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }

        } else {
            // Login failed: show error message
            request.setAttribute("errorMessage", "Email or password is incorrect!");
            request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Login Controller - Fashion Management System";
    }
}
