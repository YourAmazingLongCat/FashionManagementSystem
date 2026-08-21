package Controllers;

import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

import DALs.AccountDAO;
import Models.Account;
import Utils.passwordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ChangePasswordController", urlPatterns = {"/change-password"})
public class ChangePasswordController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.getAttribute("USER") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
        request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        HttpSession session = request.getSession();
        Account currentUser = (Account) session.getAttribute("USER");

        if (currentUser == null) {
            if (isAjax) {
                sendJsonResponse(response, false, "Session expired. Please log in again.");
            } else {
                response.sendRedirect(request.getContextPath() + "/auth/login");
            }
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            handleResponse(request, response, isAjax, false, "Please fill in all fields!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            handleResponse(request, response, isAjax, false, "Passwords do not match!");
            return;
        }

        if (newPassword.equals(oldPassword)) {
            handleResponse(request, response, isAjax, false, "New password must be different from current password!");
            return;
        }

        if (newPassword.length() < 8) {
            handleResponse(request, response, isAjax, false, "New password must be at least 8 characters!");
            return;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            handleResponse(request, response, isAjax, false, "New password must contain at least 1 uppercase, 1 lowercase and 1 digit!");
            return;
        }

        AccountDAO accountDAO = new AccountDAO();
        Account accountInDb = accountDAO.getAccountById(currentUser.getAccountId());

        if (accountInDb == null || accountInDb.getPassword() == null
                || !BCrypt.checkpw(oldPassword, accountInDb.getPassword())) {
            handleResponse(request, response, isAjax, false, "Current password is incorrect!");
            return;
        }

        String hashedPassword = passwordUtil.hashPassword(newPassword);
        boolean isUpdated = accountDAO.updatePassword(currentUser.getAccountId(), hashedPassword);

        if (isUpdated) {
            currentUser.setPassword(hashedPassword);
            session.setAttribute("USER", currentUser);
            handleResponse(request, response, isAjax, true, "Password changed successfully!");
        } else {
            handleResponse(request, response, isAjax, false, "Database error while updating password!");
        }
    }

    private void handleResponse(HttpServletRequest request, HttpServletResponse response, boolean isAjax, boolean success, String message) throws ServletException, IOException {
        if (isAjax) {
            sendJsonResponse(response, success, message);
        } else {
            if (success) {
                request.setAttribute("success", message);
            } else {
                request.setAttribute("error", message);
            }
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String safeMessage = message.replace("\"", "\\\""); 
        response.getWriter().write("{\"success\":" + success + ", \"message\":\"" + safeMessage + "\"}");
    }
}