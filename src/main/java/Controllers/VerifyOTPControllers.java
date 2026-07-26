/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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
        // Mở trang nhập OTP
        request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Lấy mã OTP người dùng nhập vào
        String inputOTP = request.getParameter("otpCode");
        HttpSession session = request.getSession();
        
        // 2. Kiểm tra mode: forgot hay register
        String mode = request.getParameter("mode");
        boolean isForgotPassword = "forgot".equals(mode);
        
        // 3. Lấy mã OTP hệ thống đã tạo ra lưu trong Session
        String generatedOTP;
        if (isForgotPassword) {
            generatedOTP = (String) session.getAttribute("forgotPasswordOTP");
            Long expiryTime = (Long) session.getAttribute("forgotPasswordOTPExpiry");
            
            if (generatedOTP == null) {
                request.setAttribute("errorMessage", "Phiên làm việc đã hết hạn. Vui lòng bắt đầu lại!");
                request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
                return;
            }
            
            // Kiểm tra OTP hết hạn chưa
            if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
                session.removeAttribute("forgotPasswordOTP");
                session.removeAttribute("forgotPasswordEmail");
                session.removeAttribute("forgotPasswordOTPExpiry");
                request.setAttribute("errorMessage", "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới!");
                request.getRequestDispatcher("/Pages/Authentication/ForgotPassword/ForgotPassword.jsp").forward(request, response);
                return;
            }
        } else {
            generatedOTP = (String) session.getAttribute("generatedOTP");
            if (generatedOTP == null) {
                request.setAttribute("errorMessage", "Phiên làm việc đã hết hạn. Vui lòng đăng ký lại!");
                request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                return;
            }
        }

        // 4. SO SÁNH 2 MÃ OTP
        if (inputOTP != null && inputOTP.equals(generatedOTP)) {
            
            if (isForgotPassword) {
                // ✅ FORGOT PASSWORD: Xác thực thành công -> Đánh dấu đã xác thực và chuyển đến reset password
                session.setAttribute("forgotPasswordOTPVerified", true);
                // Xóa OTP cũ để tránh reuse
                session.removeAttribute("forgotPasswordOTP");
                session.removeAttribute("forgotPasswordOTPExpiry");
                
                response.sendRedirect(request.getContextPath() + "/auth/reset-password");
            } else {
                // ✅ REGISTER: MÃ KHỚP! BẮT ĐẦU LƯU VÀO DATABASE NÈ:
                
                // Lấy lại dữ liệu tạm từ Session
                String fullName = (String) session.getAttribute("tempName");
                String email = (String) session.getAttribute("tempEmail");
                String phone = (String) session.getAttribute("tempPhone");
                String password = (String) session.getAttribute("tempPassword");

                Connection conn = null;
                PreparedStatement psInsert = null;

                try {
                    conn = new DBContext().getConnection();
                    
                    // Tạo accountId dùng AccountDAO pattern
                    String newAccountId = new AccountDAO().generateNextAccountId();

                    // Lưu vào bảng Accounts - dùng username = email như AccountDAO
                    String insertSQL = "INSERT INTO Accounts (accountId, username, email, passwordHash, fullName, role, status, phone) VALUES (?, ?, ?, ?, ?, 'Customer', 'Active', ?)";
                    
                    psInsert = conn.prepareStatement(insertSQL);
                    psInsert.setString(1, newAccountId);
                    psInsert.setString(2, email); // username = email
                    psInsert.setString(3, email);
                    psInsert.setString(4, passwordUtil.hashPassword(password));
                    psInsert.setString(5, fullName);
                    psInsert.setString(6, phone);

                    int row = psInsert.executeUpdate();
                    
                    if (row > 0) {
                        // LƯU THÀNH CÔNG -> Dọn dẹp sạch sẽ Session
                        session.removeAttribute("tempName");
                        session.removeAttribute("tempEmail");
                        session.removeAttribute("tempPhone");
                        session.removeAttribute("tempPassword");
                        session.removeAttribute("generatedOTP");

                        // Đá về trang Login với thông báo xanh lá
                        request.setAttribute("successMessage", "Xác thực thành công! Hệ thống đã ghi nhận tài khoản. Hãy đăng nhập.");
                        request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
                    } else {
                        request.setAttribute("errorMessage", "Lỗi hệ thống khi lưu tài khoản.");
                        request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                    }
                } catch (Exception e) {
                    request.setAttribute("errorMessage", "Lỗi CSDL: " + e.getMessage());
                    request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
                } finally {
                    try { if (psInsert != null) psInsert.close(); if (conn != null) conn.close(); } catch (Exception ex) {}
                }
            }
            
        } else {
            // Nhập sai mã OTP -> Bắn lỗi, ở lại trang nhập OTP
            if (isForgotPassword) {
                request.setAttribute("errorMessage", "Mã OTP không chính xác. Vui lòng kiểm tra lại hòm thư!");
                response.sendRedirect(request.getContextPath() + "/auth/verify-otp?mode=forgot");
            } else {
                request.setAttribute("errorMessage", "Mã OTP không chính xác. Vui lòng kiểm tra lại hòm thư!");
                request.getRequestDispatcher("/Pages/Authentication/Register/VerifyOTP.jsp").forward(request, response);
            }
        }
    }
}