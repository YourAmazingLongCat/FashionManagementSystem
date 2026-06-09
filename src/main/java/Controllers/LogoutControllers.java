package Controllers;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LogoutControllers", urlPatterns = {"/auth/logout"})
public class LogoutControllers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Lấy session hiện tại (truyền false để không tạo session mới nếu nó không tồn tại)
        HttpSession session = request.getSession(false);
        
        // 2. Nếu session đang tồn tại, tiến hành hủy nó đi
        if (session != null) {
            session.invalidate();
        }
        
        // 3. Sau khi xóa phiên đăng nhập, đá người dùng về lại trang chủ
        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Form đăng xuất nếu xài POST thì cũng gọi chung hàm doGet cho lẹ
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Logout Controller - Fashion Management System";
    }
}