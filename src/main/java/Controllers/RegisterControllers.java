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

        // 1. Lấy dữ liệu từ Form
        String fullName = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phoneNumber"); 
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // 2. Kiểm tra mật khẩu khớp
        if (password != null && !password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
            return;
        }

        // =========================================================================
        // 👉 ĐÃ THÊM: Kiểm tra độ mạnh của mật khẩu (Chữ hoa đầu, có số, có ký tự ĐB)
        // =========================================================================
        String passwordPattern = "^[A-Z](?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$";
        
        if (password == null || !password.matches(passwordPattern)) {
            request.setAttribute("errorMessage", "Mật khẩu phải bắt đầu bằng chữ cái viết hoa, có ít nhất 1 chữ số và 1 ký tự đặc biệt!");
            request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
            return;
        }
        // =========================================================================

        Connection connection = null; 
        PreparedStatement psCheck = null;
        ResultSet rs = null;

        try {
            connection = new DBContext().getConnection(); 
            System.out.println("Connection = " + connection);
            
            // 3. Kiểm tra trùng lặp (Email hoặc Phone)
            String checkSQL = "SELECT email FROM Accounts WHERE email = ? OR phone = ?";
            psCheck = connection.prepareStatement(checkSQL);
            psCheck.setString(1, email);
            psCheck.setString(2, phone);
            rs = psCheck.executeQuery();

            if (rs.next()) {
                request.setAttribute("errorMessage", "Email hoặc Số điện thoại đã được đăng ký!");
                request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
                return;
            }

            // 4. Sinh ngẫu nhiên mã OTP 6 chữ số
            Random rand = new Random();
            String otpCode = String.format("%06d", rand.nextInt(999999));

            // 5. Gửi email chứa OTP thông qua EmailUtils
            boolean mailSent = EmailUtils.sendOTP(email, otpCode);
            
            if (mailSent) {
                // Nếu gửi thành công, lưu dữ liệu tạm thời vào Session
                HttpSession session = request.getSession();
                session.setAttribute("tempName", fullName);
                session.setAttribute("tempEmail", email);
                session.setAttribute("tempPhone", phone);
                session.setAttribute("tempPassword", password);
                session.setAttribute("generatedOTP", otpCode);
                
                // Chuyển hướng sang Servlet xử lý trang nhập OTP
                response.sendRedirect(request.getContextPath() + "/auth/verify-otp");
            } else {
                request.setAttribute("errorMessage", "Không thể gửi mã xác minh tới Email này. Vui lòng kiểm tra lại cấu hình SMTP hoặc mạng!");
                request.getRequestDispatcher("/Pages/Authentication/Register/Register.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi Hệ Thống: " + e.getMessage());
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