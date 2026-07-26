package Controllers;

import DALs.AccountDAO;
import Utils.EmailUtils;
import Models.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/auth/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        HttpSession session = request.getSession();

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập địa chỉ email!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }

        email = email.trim().toLowerCase();

        // Kiểm tra email có tồn tại trong hệ thống không
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountByEmail(email);

        if (account == null) {
            // Không tiết lộ email có tồn tại hay không vì lý do bảo mật
            // Vẫn hiển thị thông báo thành công để tránh email enumeration
            request.setAttribute("successMessage", "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }

        // Tạo OTP mới
        String otpCode = generateOTP();
        long expiryTime = System.currentTimeMillis() + (OTP_EXPIRY_MINUTES * 60 * 1000);

        // Lưu OTP và thông tin vào session
        session.setAttribute("forgotPasswordOTP", otpCode);
        session.setAttribute("forgotPasswordEmail", email);
        session.setAttribute("forgotPasswordOTPExpiry", expiryTime);

        // Gửi email OTP
        boolean emailSent = EmailUtils.sendOTPForPasswordReset(email, otpCode, OTP_EXPIRY_MINUTES);

        if (emailSent) {
            // Chuyển hướng đến trang nhập OTP
            response.sendRedirect(request.getContextPath() + "/auth/verify-otp?mode=forgot");
        } else {
            request.setAttribute("errorMessage", "Không thể gửi email. Vui lòng thử lại sau!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
        }
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
