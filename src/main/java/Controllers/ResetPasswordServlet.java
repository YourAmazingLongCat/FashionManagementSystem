package Controllers;

import DALs.AccountDAO;
import Models.Account;
import Utils.passwordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/auth/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        // Check OTP verification status
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/forgot-password");
            return;
        }
        
        Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOTPVerified");
        String email = (String) session.getAttribute("forgotPasswordEmail");
        
        if (otpVerified == null || !otpVerified || email == null) {
            response.sendRedirect(request.getContextPath() + "/auth/forgot-password");
            return;
        }
        
        request.setAttribute("email", email);
        request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Check OTP verification status
        if (session == null) {
            request.setAttribute("errorMessage", "Invalid session. Please start again!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }
        
        Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOTPVerified");
        String email = (String) session.getAttribute("forgotPasswordEmail");
        
        if (otpVerified == null || !otpVerified || email == null) {
            request.setAttribute("errorMessage", "Invalid session. Please start again!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validation
        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please enter a new password!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        if (newPassword.length() < 8) {
            request.setAttribute("errorMessage", "Password must be at least 8 characters!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        // Update password
        AccountDAO accountDAO = new AccountDAO();
        String hashedPassword = passwordUtil.hashPassword(newPassword);

        // Get accountId from email
        Account account = accountDAO.getAccountByEmail(email);
        if (account == null) {
            request.setAttribute("errorMessage", "Account does not exist!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        boolean updated = accountDAO.updatePassword(account.getAccountId(), hashedPassword);

        if (updated) {
            // Cleanup session
            session.removeAttribute("forgotPasswordOTP");
            session.removeAttribute("forgotPasswordEmail");
            session.removeAttribute("forgotPasswordOTPExpiry");
            session.removeAttribute("forgotPasswordOTPVerified");

            request.setAttribute("successMessage", "Password reset successful! Please log in with your new password.");
            request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "Cannot update password. Please try again!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
        }
    }
}
