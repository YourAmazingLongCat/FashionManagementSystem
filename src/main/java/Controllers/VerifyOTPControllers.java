package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                PreparedStatement psLock = null;

                try {
                    conn = new DBContext().getConnection();
                    conn.setAutoCommit(false);

                    String newAccountId = null;
                    int maxRetries = 5;

                    for (int attempt = 0; attempt < maxRetries; attempt++) {
                        // Lock the Customers table with UPDLOCK to prevent concurrent reads
                        psLock = conn.prepareStatement(
                            "SELECT TOP 1 customerId FROM Customers WITH (UPDLOCK) ORDER BY customerId DESC");
                        ResultSet rsLock = psLock.executeQuery();

                        String lastId = null;
                        if (rsLock.next()) {
                            lastId = rsLock.getString(1);
                        }
                        rsLock.close();
                        psLock.close();
                        psLock = null;

                        String numericPart = lastId != null ? lastId.replaceAll("[^0-9]", "") : "";
                        long nextNum = numericPart.isEmpty() ? 1 : Long.parseLong(numericPart) + 1;
                        newAccountId = "ACC" + String.format("%05d", nextNum);

                        // Try to insert — if duplicate key, loop will retry with next ID
                        String insertSQL = "INSERT INTO Customers (customerId, username, email, passwordHash, fullName, status, phone) VALUES (?, ?, ?, ?, ?, 'Active', ?)";
                        psInsert = conn.prepareStatement(insertSQL);
                        psInsert.setString(1, newAccountId);
                        psInsert.setString(2, email);
                        psInsert.setString(3, email);
                        psInsert.setString(4, passwordUtil.hashPassword(password));
                        psInsert.setString(5, fullName);
                        psInsert.setString(6, phone);

                        try {
                            int row = psInsert.executeUpdate();
                            psInsert.close();
                            psInsert = null;
                            conn.commit();
                            break;
                        } catch (SQLException insertEx) {
                            psInsert.close();
                            psInsert = null;
                            if (insertEx.getMessage() != null && insertEx.getMessage().contains("duplicate key")) {
                                continue; // Retry with next ID
                            }
                            throw insertEx;
                        }
                    }

                    if (newAccountId == null) {
                        conn.rollback();
                        request.setAttribute("errorMessage", "Unable to generate a unique account ID after multiple attempts. Please try again.");
                        request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                        return;
                    }

                    session.removeAttribute("tempName");
                    session.removeAttribute("tempEmail");
                    session.removeAttribute("tempPhone");
                    session.removeAttribute("tempPassword");
                    session.removeAttribute("generatedOTP");

                    request.setAttribute("successMessage", "Verification successful! Your account is ready. Please log in.");
                    request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
                } catch (Exception e) {
                    if (conn != null) {
                        try { conn.rollback(); } catch (Exception ignored) {}
                    }
                    request.setAttribute("errorMessage", "Database error: " + e.getMessage());
                    request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                } finally {
                    try {
                        if (psInsert != null) psInsert.close();
                        if (psLock != null) psLock.close();
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (Exception ex) {}
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
