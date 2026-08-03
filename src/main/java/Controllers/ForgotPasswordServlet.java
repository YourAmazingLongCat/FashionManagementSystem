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
            request.setAttribute("errorMessage", "Please enter your email address!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }

        email = email.trim().toLowerCase();

        // Check if email exists in system
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountByEmail(email);

        if (account == null) {
            // Don't reveal if email exists (security: prevent email enumeration).
            // Still show success message to avoid leaking info.
            request.setAttribute("successMessage", "If the email exists, an OTP has been sent!");
            request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
            return;
        }

        // Generate new OTP
        String otpCode = generateOTP();
        long expiryTime = System.currentTimeMillis() + (OTP_EXPIRY_MINUTES * 60 * 1000);

        // Save OTP and email to session
        session.setAttribute("forgotPasswordOTP", otpCode);
        session.setAttribute("forgotPasswordEmail", email);
        session.setAttribute("forgotPasswordOTPExpiry", expiryTime);

        // Send OTP email
        boolean emailSent = EmailUtils.sendOTPForPasswordReset(email, otpCode, OTP_EXPIRY_MINUTES);

        if (emailSent) {
            // Redirect to OTP input page
            response.sendRedirect(request.getContextPath() + "/auth/verify-otp?mode=forgot");
        } else {
            request.setAttribute("errorMessage", "Cannot send email. Please try again later!");
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
