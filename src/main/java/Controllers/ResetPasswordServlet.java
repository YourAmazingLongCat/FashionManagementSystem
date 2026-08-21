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
        
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/forgot-password");
            return;
        }
        
        Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOTPVerified");
        Boolean allowReset = (Boolean) session.getAttribute("allowResetPassword"); 
        boolean isVerified = (otpVerified != null && otpVerified) || (allowReset != null && allowReset);
        
        String email = (String) session.getAttribute("forgotPasswordEmail");
        
        if (!isVerified || email == null) {
            response.sendRedirect(request.getContextPath() + "/auth/forgot-password");
            return;
        }
        
        request.setAttribute("email", email);
        request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            sendResponse(request, response, isAjax, false, "Invalid session. Please start again!", "/Pages/Authentication/ForgotPassword/ForgotPassword.jsp");
            return;
        }
        
        Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOTPVerified");
        Boolean allowReset = (Boolean) session.getAttribute("allowResetPassword");
        boolean isVerified = (otpVerified != null && otpVerified) || (allowReset != null && allowReset);
        String email = (String) session.getAttribute("forgotPasswordEmail");
        
        if (!isVerified || email == null) {
            sendResponse(request, response, isAjax, false, "Session expired or invalid. Please request a new OTP.", "/Pages/Authentication/ForgotPassword/ForgotPassword.jsp");
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (newPassword == null || newPassword.trim().isEmpty()) {
            sendResponse(request, response, isAjax, false, "Please enter a new password!", "/Pages/Authentication/ForgotPassword/ResetPassword.jsp");
            return;
        }

        if (newPassword.length() < 8) {
            sendResponse(request, response, isAjax, false, "Password must be at least 8 characters!", "/Pages/Authentication/ForgotPassword/ResetPassword.jsp");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            sendResponse(request, response, isAjax, false, "Passwords do not match!", "/Pages/Authentication/ForgotPassword/ResetPassword.jsp");
            return;
        }

        AccountDAO accountDAO = new AccountDAO();
        String hashedPassword = passwordUtil.hashPassword(newPassword);

        Account account = accountDAO.getAccountByEmail(email);
        if (account == null) {
            sendResponse(request, response, isAjax, false, "Account does not exist!", "/Pages/Authentication/ForgotPassword/ResetPassword.jsp");
            return;
        }

        boolean updated = accountDAO.updatePassword(account.getAccountId(), hashedPassword);

        if (updated) {
            session.removeAttribute("forgotPasswordOTP");
            session.removeAttribute("forgotPasswordEmail");
            session.removeAttribute("forgotPasswordOTPExpiry");
            session.removeAttribute("forgotPasswordOTPVerified");
            session.removeAttribute("allowResetPassword");

            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":true, \"message\":\"Password reset successful!\"}");
            } else {
                request.setAttribute("successMessage", "Password reset successful! Please log in with your new password.");
                request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
            }
        } else {
            sendResponse(request, response, isAjax, false, "Cannot update password. Please try again!", "/Pages/Authentication/ForgotPassword/ResetPassword.jsp");
        }
    }

    private void sendResponse(HttpServletRequest request, HttpServletResponse response, boolean isAjax, boolean success, String message, String fallbackJsp) throws ServletException, IOException {
        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":" + success + ", \"message\":\"" + message + "\"}");
        } else {
            request.setAttribute("errorMessage", message);
            request.getRequestDispatcher(fallbackJsp).forward(request, response);
        }
    }
}