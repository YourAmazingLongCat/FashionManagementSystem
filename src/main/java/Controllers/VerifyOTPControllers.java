package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import Utils.DBContext;
import Utils.passwordUtil;
import DALs.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "VerifyOTPControllers", urlPatterns = {"/auth/verify-otp"})
public class VerifyOTPControllers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Show OTP input page
        request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Read OTP from user
        String inputOTP = request.getParameter("otpCode");
        HttpSession session = request.getSession();

        // 2. Check mode: forgot or register
        String mode = request.getParameter("mode");
        boolean isForgotPassword = "forgot".equals(mode);

        // 3. Get OTP stored in session
        String generatedOTP;
        if (isForgotPassword) {
            generatedOTP = (String) session.getAttribute("forgotPasswordOTP");
            Long expiryTime = (Long) session.getAttribute("forgotPasswordOTPExpiry");

            if (generatedOTP == null) {
                request.setAttribute("errorMessage", "Session expired. Please start again!");
                request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
                return;
            }

            // Check OTP expiry
            if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
                session.removeAttribute("forgotPasswordOTP");
                session.removeAttribute("forgotPasswordEmail");
                session.removeAttribute("forgotPasswordOTPExpiry");
                request.setAttribute("errorMessage", "OTP expired. Please request a new one!");
                request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
                return;
            }
        } else {
            generatedOTP = (String) session.getAttribute("generatedOTP");
            if (generatedOTP == null) {
                request.setAttribute("errorMessage", "Session expired. Please register again!");
                request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                return;
            }
        }

        // 4. Compare OTPs
        if (inputOTP != null && inputOTP.equals(generatedOTP)) {

            if (isForgotPassword) {
                // Forgot password: mark verified, redirect to reset password
                session.setAttribute("forgotPasswordOTPVerified", true);
                // Remove used OTP to prevent reuse
                session.removeAttribute("forgotPasswordOTP");
                session.removeAttribute("forgotPasswordOTPExpiry");

                response.sendRedirect(request.getContextPath() + "/auth/reset-password");
            } else {
                // Register: OTP matched, save new account to DB

                // Get temp data from session
                String fullName = (String) session.getAttribute("tempName");
                String email = (String) session.getAttribute("tempEmail");
                String phone = (String) session.getAttribute("tempPhone");
                String password = (String) session.getAttribute("tempPassword");

                Connection conn = null;
                PreparedStatement psInsert = null;

                try {
                    conn = new DBContext().getConnection();

                    // Generate accountId using AccountDAO pattern
                    String newAccountId = new AccountDAO().generateNextAccountId();

                    // Insert into Accounts (username = email, same as AccountDAO)
                    String insertSQL = "INSERT INTO Accounts (accountId, username, email, passwordHash, fullName, role, status, phone) VALUES (?, ?, ?, ?, ?, 'Customer', 'Active', ?)";

                    psInsert = conn.prepareStatement(insertSQL);
                    psInsert.setString(1, newAccountId);
                    psInsert.setString(2, email);
                    psInsert.setString(3, email);
                    psInsert.setString(4, passwordUtil.hashPassword(password));
                    psInsert.setString(5, fullName);
                    psInsert.setString(6, phone);

                    int row = psInsert.executeUpdate();

                    if (row > 0) {
                        // Save success: cleanup session
                        session.removeAttribute("tempName");
                        session.removeAttribute("tempEmail");
                        session.removeAttribute("tempPhone");
                        session.removeAttribute("tempPassword");
                        session.removeAttribute("generatedOTP");

                        // Redirect to login with success message
                        request.setAttribute("successMessage", "Verification successful! Your account is ready. Please log in.");
                        request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
                    } else {
                        request.setAttribute("errorMessage", "System error while saving account.");
                        request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                    }
                } catch (Exception e) {
                    request.setAttribute("errorMessage", "Database error: " + e.getMessage());
                    request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                } finally {
                    try { if (psInsert != null) psInsert.close(); if (conn != null) conn.close(); } catch (Exception ex) {}
                }
            }

        } else {
            // Wrong OTP: stay on OTP page with error
            if (isForgotPassword) {
                request.setAttribute("errorMessage", "OTP is incorrect. Please check your inbox!");
                response.sendRedirect(request.getContextPath() + "/auth/verify-otp?mode=forgot");
            } else {
                request.setAttribute("errorMessage", "OTP is incorrect. Please check your inbox!");
                request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
            }
        }
    }
}
