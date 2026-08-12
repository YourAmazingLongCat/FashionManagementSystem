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

        // Check login (security)
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

        // 1. Get current user from session
        HttpSession session = request.getSession();
        Account currentUser = (Account) session.getAttribute("USER");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Read form data
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // 3. Check empty fields
        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            request.setAttribute("error", "Please fill in all fields!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 4. Check confirm password match
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 5. Check new password != old password
        if (newPassword.equals(oldPassword)) {
            request.setAttribute("error", "New password must be different from current password!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 6. Check password strength (min 8 chars + uppercase + lowercase + digit)
        if (newPassword.length() < 8) {
            request.setAttribute("error", "New password must be at least 8 characters!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            request.setAttribute("error", "New password must contain at least 1 uppercase, 1 lowercase and 1 digit!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 7. Get hashed password from DB to compare
        AccountDAO accountDAO = new AccountDAO();
        Account accountInDb = accountDAO.getAccountById(currentUser.getAccountId());

        if (accountInDb == null || accountInDb.getPassword() == null
                || !BCrypt.checkpw(oldPassword, accountInDb.getPassword())) {
            request.setAttribute("error", "Current password is incorrect!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 8. Update new password into database
        String hashedPassword = passwordUtil.hashPassword(newPassword);
        boolean isUpdated = accountDAO.updatePassword(currentUser.getAccountId(), hashedPassword);

        if (isUpdated) {
            request.setAttribute("success", "Password changed successfully!");
            currentUser.setPassword(hashedPassword);
            session.setAttribute("USER", currentUser);
        } else {
            request.setAttribute("error", "Database error while updating password!");
        }

        // 9. Always return to the JSP to show message
        request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
        request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }
}
