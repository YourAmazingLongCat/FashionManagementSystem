package Controllers;

import DAOs.AccountDAO;
import Models.Account;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller xử lý logic Đăng Nhập cho Fashion Store
 */
@WebServlet(name = "LoginControllers", urlPatterns = {"/auth/login"})
public class LoginControllers extends HttpServlet {

    /**
     * Xử lý phương thức GET: Mở trang web đăng nhập
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Điều hướng tới file Login.jsp
        request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
    }

    /**
     * Xử lý phương thức POST: Khi người dùng bấm nút "Đăng Nhập"
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy dữ liệu từ form (khớp với thuộc tính name="email" và name="password" trong JSP)
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 2. Gọi DAO để kiểm tra DB
        AccountDAO dao = new AccountDAO();
        Account acc = dao.checkLogin(email, password);

        // 3. Xử lý kết quả
        if (acc != null) {

            // Kiểm tra status kiểu String
            // Nếu status khác null và KHÔNG bằng chữ "Active" (không phân biệt hoa thường)
            if (acc.getStatus() != null && !acc.getStatus().equalsIgnoreCase("Active")) {
                request.setAttribute("errorMessage", "Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt!");
                request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
                return;
            }

            // Đăng nhập thành công: Lưu thông tin Account vào Session
            HttpSession session = request.getSession();
            session.setAttribute("USER", acc);

            // 👉 ĐÃ SỬA Ở ĐÂY: Phân luồng chuyển hướng rõ ràng cho từng Role
            String role = acc.getRole();
            if (role != null && role.equalsIgnoreCase("Admin")) {
                // Nếu là Admin -> Vào trang Quản trị
                // Lưu ý: Đảm bảo urlPatterns của trang Admin khớp với "/admin" nhé!
                response.sendRedirect(request.getContextPath() + "/admin");
                
            } else if (role != null && role.equalsIgnoreCase("Staff")) {
                // Nếu là Staff -> Vào trang Quản lý Sản phẩm
                response.sendRedirect(request.getContextPath() + "/staff/products");
                
            } else {
                // Mặc định (Customer) -> Về trang chủ Home
                response.sendRedirect(request.getContextPath() + "/home");
            }

        } else {
            // Đăng nhập thất bại: Gửi thông báo lỗi về lại trang Login
            request.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác!");
            request.getRequestDispatcher("/Pages/Authentication/Login/Login.jsp").forward(request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Login Controller - Fashion Management System";
    }
}