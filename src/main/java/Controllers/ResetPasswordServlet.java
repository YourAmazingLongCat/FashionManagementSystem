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
        
        // Kiểm tra đã xác thực OTP chưa
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
        
        // Kiểm tra đã xác thực OTP chưa
        if (session == null) {
            request.setAttribute("errorMessage", "Phiên làm việc không hợp lệ. Vui lòng bắt đầu lại!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }
        
        Boolean otpVerified = (Boolean) session.getAttribute("forgotPasswordOTPVerified");
        String email = (String) session.getAttribute("forgotPasswordEmail");
        
        if (otpVerified == null || !otpVerified || email == null) {
            request.setAttribute("errorMessage", "Phiên làm việc không hợp lệ. Vui lòng bắt đầu lại!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validation
        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập mật khẩu mới!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        if (newPassword.length() < 8) {
            request.setAttribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }

        // Cập nhật password
        AccountDAO accountDAO = new AccountDAO();
        String hashedPassword = passwordUtil.hashPassword(newPassword);
        
        // Lấy accountId từ email
        Account account = accountDAO.getAccountByEmail(email);
        if (account == null) {
            request.setAttribute("errorMessage", "Tài khoản không tồn tại!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
            return;
        }
        
        boolean updated = accountDAO.updatePassword(account.getAccountId(), hashedPassword);

        if (updated) {
            // Dọn dẹp session
            session.removeAttribute("forgotPasswordOTP");
            session.removeAttribute("forgotPasswordEmail");
            session.removeAttribute("forgotPasswordOTPExpiry");
            session.removeAttribute("forgotPasswordOTPVerified");
            
            request.setAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
            request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "Không thể cập nhật mật khẩu. Vui lòng thử lại!");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ResetPassword.jsp").forward(request, response);
        }
    }
}
