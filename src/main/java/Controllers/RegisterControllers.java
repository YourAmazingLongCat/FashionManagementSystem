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

        // 1. Read form data
        String fullName = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phoneNumber");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // 2. Check confirm password match
        if (password != null && !password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match!");
            request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
            return;
        }

        // Check password strength: starts with uppercase, has digit and special char
        String passwordPattern = "^[A-Z](?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$";

        if (password == null || !password.matches(passwordPattern)) {
            request.setAttribute("errorMessage", "Password must start with uppercase letter and contain at least 1 digit and 1 special character!");
            request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
            return;
        }

        Connection connection = null;
        PreparedStatement psCheck = null;
        ResultSet rs = null;

        try {
            connection = new DBContext().getConnection();
            System.out.println("Connection = " + connection);

            // 3. Check duplicate (email or phone)
            String checkSQL = "SELECT email FROM Accounts WHERE email = ? OR phone = ?";
            psCheck = connection.prepareStatement(checkSQL);
            psCheck.setString(1, email);
            psCheck.setString(2, phone);
            rs = psCheck.executeQuery();

            if (rs.next()) {
                request.setAttribute("errorMessage", "Email or phone is already registered!");
                request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
                return;
            }

            // 4. Generate random 6-digit OTP
            Random rand = new Random();
            String otpCode = String.format("%06d", rand.nextInt(999999));

            // 5. Send OTP email via EmailUtils
            boolean mailSent = EmailUtils.sendOTP(email, otpCode);

            if (mailSent) {
                // Save temp data in session for the OTP verify step
                HttpSession session = request.getSession();
                session.setAttribute("tempName", fullName);
                session.setAttribute("tempEmail", email);
                session.setAttribute("tempPhone", phone);
                session.setAttribute("tempPassword", password);
                session.setAttribute("generatedOTP", otpCode);

                // Redirect to OTP input page
                response.sendRedirect(request.getContextPath() + "/auth/verify-otp");
            } else {
                request.setAttribute("errorMessage", "Cannot send OTP email. Please check SMTP config or network!");
                request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error: " + e.getMessage());
            request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
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
}