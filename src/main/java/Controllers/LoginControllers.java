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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        AccountDAO dao = new AccountDAO();
        Account acc = dao.checkLogin(email, password);

        if (acc != null) {
            if (acc.getStatus() != null && !acc.getStatus().equalsIgnoreCase("Active")) {
                if (isAjax) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":false, \"message\":\"Your account is locked or not activated!\"}");
                    return;
                }
                request.setAttribute("errorMessage", "Your account is locked or not activated!");
                request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("USER", acc);

            String redirectUrl = request.getContextPath() + "/home";
            String role = acc.getRole();
            if (role != null && role.equalsIgnoreCase("Staff")) {
                redirectUrl = request.getContextPath() + "/staff/products";
            } else if (role != null && role.equalsIgnoreCase("Admin")) {
                redirectUrl = request.getContextPath() + "/Admin";
            }

            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":true, \"redirectUrl\":\"" + redirectUrl + "\"}");
                return;
            }
            
            response.sendRedirect(redirectUrl);

        } else {
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false, \"message\":\"Email or password is incorrect!\"}");
                return;
            }
            request.setAttribute("errorMessage", "Email or password is incorrect!");
            request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Login Controller - Fashion Management System";
    }
}