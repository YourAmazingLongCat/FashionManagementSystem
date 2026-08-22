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
        request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String inputOTP = request.getParameter("otp");
        if (inputOTP == null) {
            inputOTP = request.getParameter("otpCode");
        }
        
        HttpSession session = request.getSession();

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        String mode = request.getParameter("mode");
        boolean isForgotPassword = "forgot".equals(mode);

        String generatedOTP;
        if (isForgotPassword) {
            generatedOTP = (String) session.getAttribute("forgotPasswordOTP");
            Long expiryTime = (Long) session.getAttribute("forgotPasswordOTPExpiry");

            if (generatedOTP == null) {
                sendResponse(request, response, isAjax, false, "Session expired. Please start again!", "/Pages/Authentication/ForgotPassword/ForgotPassword.jsp");
                return;
            }

            if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
                session.removeAttribute("forgotPasswordOTP");
                session.removeAttribute("forgotPasswordEmail");
                session.removeAttribute("forgotPasswordOTPExpiry");
                sendResponse(request, response, isAjax, false, "OTP expired. Please request a new one!", "/Pages/Authentication/ForgotPassword/ForgotPassword.jsp");
                return;
            }
        } else {
            generatedOTP = (String) session.getAttribute("generatedOTP");
            if (generatedOTP == null) {
                sendResponse(request, response, isAjax, false, "Session expired. Please register again!", "/Pages/Authentication/Register/VerifyOTP.jsp");
                return;
            }
        }

        if (inputOTP != null && inputOTP.equals(generatedOTP)) {

            if (isForgotPassword) {
                session.setAttribute("forgotPasswordOTPVerified", true);
                session.setAttribute("allowResetPassword", true); 
                
                session.removeAttribute("forgotPasswordOTP");
                session.removeAttribute("forgotPasswordOTPExpiry");

                sendResponse(request, response, isAjax, true, null, request.getContextPath() + "/auth/reset-password");
            } else {
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
                                continue; 
                            }
                            throw insertEx;
                        }
                    }

                    if (newAccountId == null) {
                        conn.rollback();
                        sendResponse(request, response, isAjax, false, "Unable to generate a unique account ID after multiple attempts. Please try again.", "/Pages/Authentication/Register/VerifyOTP.jsp");
                        return;
                    }

                    session.removeAttribute("tempName");
                    session.removeAttribute("tempEmail");
                    session.removeAttribute("tempPhone");
                    session.removeAttribute("tempPassword");
                    session.removeAttribute("generatedOTP");

                    sendResponse(request, response, isAjax, true, "Registration successful! You can now login.", "/Pages/Authentication/Login/Login.jsp");

                } catch (Exception e) {
                    if (conn != null) {
                        try { conn.rollback(); } catch (Exception ignored) {}
                    }
                    sendResponse(request, response, isAjax, false, "Database error: " + e.getMessage(), "/Pages/Authentication/Register/VerifyOTP.jsp");
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
            if (isForgotPassword) {
                sendResponse(request, response, isAjax, false, "OTP is incorrect. Please check your inbox!", "/auth/verify-otp?mode=forgot");
            } else {
                sendResponse(request, response, isAjax, false, "OTP is incorrect. Please check your inbox!", "/Pages/Authentication/Register/VerifyOTP.jsp");
            }
        }
    }

    private void sendResponse(HttpServletRequest request, HttpServletResponse response, boolean isAjax, boolean success, String message, String redirectUrl) throws ServletException, IOException {
        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if (success) {
                response.getWriter().write("{\"success\":true, \"redirectUrl\":\"" + redirectUrl + "\"}");
            } else {
                String safeMessage = message != null ? message.replace("\"", "\\\"") : "";
                response.getWriter().write("{\"success\":false, \"message\":\"" + safeMessage + "\"}");
            }
        } else {
            if (success) {
                if (redirectUrl != null && !redirectUrl.endsWith(".jsp")) {
                    response.sendRedirect(redirectUrl);
                } else if (redirectUrl != null) {
                    request.setAttribute("successMessage", message);
                    request.getRequestDispatcher(redirectUrl).forward(request, response);
                }
            } else {
                request.setAttribute("errorMessage", message);
                if (redirectUrl != null && redirectUrl.endsWith(".jsp")) {
                    request.getRequestDispatcher(redirectUrl).forward(request, response);
                } else if (redirectUrl != null) {
                    response.sendRedirect(request.getContextPath() + redirectUrl);
                }
            }
        }
    }
}