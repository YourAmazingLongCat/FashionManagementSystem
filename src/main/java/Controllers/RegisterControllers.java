package Controllers;

import Utils.DBContext;
import Utils.EmailUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "RegisterControllers", urlPatterns = {"/auth/register"})
public class RegisterControllers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        String fullName = request.getParameter("fullName");
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = request.getParameter("name");
        }
        
        String email = request.getParameter("email");
        
        String phone = request.getParameter("phone");
        if (phone == null || phone.trim().isEmpty()) {
            phone = request.getParameter("phoneNumber");
        }
        
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password != null && !password.equals(confirmPassword)) {
            sendResponse(request, response, isAjax, false, "Passwords do not match!");
            return;
        }

        String passwordPattern = "^[A-Z](?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$";

        if (password == null || !password.matches(passwordPattern)) {
            sendResponse(request, response, isAjax, false, "Password must start with uppercase letter and contain at least 1 digit and 1 special character!");
            return;
        }

        Connection connection = null;
        PreparedStatement psCheck = null;
        ResultSet rs = null;

        try {
            connection = new DBContext().getConnection();

            String checkSQL = "SELECT email FROM ("
                    + "SELECT email, phone FROM Customers "
                    + "UNION ALL "
                    + "SELECT email, phone FROM Employees) AS a "
                    + "WHERE email = ? OR phone = ?";
            psCheck = connection.prepareStatement(checkSQL);
            psCheck.setString(1, email);
            psCheck.setString(2, phone);
            rs = psCheck.executeQuery();

            if (rs.next()) {
                sendResponse(request, response, isAjax, false, "Email or phone is already registered!");
                return;
            }

            Random rand = new Random();
            String otpCode = String.format("%06d", rand.nextInt(999999));

            boolean mailSent = EmailUtils.sendOTP(email, otpCode);

            if (mailSent) {
                HttpSession session = request.getSession();
                session.setAttribute("tempName", fullName);
                session.setAttribute("tempEmail", email);
                session.setAttribute("tempPhone", phone);
                session.setAttribute("tempPassword", password);
                session.setAttribute("generatedOTP", otpCode);

                if (isAjax) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":true}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/auth/verify-otp");
                }
            } else {
                sendResponse(request, response, isAjax, false, "Cannot send OTP email. Please check SMTP config or network!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(request, response, isAjax, false, "System error: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (psCheck != null) psCheck.close();
                if (connection != null) connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendResponse(HttpServletRequest request, HttpServletResponse response, boolean isAjax, boolean success, String message) throws ServletException, IOException {
        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String safeMessage = message.replace("\"", "\\\""); 
            response.getWriter().write("{\"success\":" + success + ", \"message\":\"" + safeMessage + "\"}");
        } else {
            request.setAttribute("errorMessage", message);
            request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
        }
    }
}