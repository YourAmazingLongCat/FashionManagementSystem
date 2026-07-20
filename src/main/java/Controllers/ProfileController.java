/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;


import java.io.IOException;

import DALs.AccountDAO;
import DALs.CategoryDAO;
import Models.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller xử lý xem và cập nhật hồ sơ cá nhân
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile", "/profile/update"})
public class ProfileController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Kiểm tra đăng nhập
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");
        
        if (user == null) {
            // Chưa đăng nhập thì đá về trang login
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Lấy categories để Header.jsp hiển thị danh mục
        request.setAttribute("categories", new CategoryDAO().getAllCategories());
        request.setAttribute("contentPage", "/Pages/Customer/Profile.jsp");

        // 3. Chuyển hướng qua layout chung
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Cấu hình UTF-8 để nhận tiếng Việt không bị lỗi font
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 1. Lấy dữ liệu text từ form
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        // 2. Cập nhật dữ liệu vào object Account hiện tại
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);

        // 4. Gọi DAO để cập nhật Database
        AccountDAO dao = new AccountDAO();
        boolean isSuccess = dao.updateProfile(user);

        if (isSuccess) {
            // Cập nhật lại session để giao diện Header cũng đổi ảnh ngay lập tức
            session.setAttribute("USER", user);
            // Gửi thông báo thành công (Bạn có thể in nó ra bằng EL trong jsp sau)
            session.setAttribute("toastMsg", "Cập nhật hồ sơ thành công!");
        } else {
            session.setAttribute("toastError", "Có lỗi xảy ra khi lưu dữ liệu!");
        }

        // 5. Redirect lại trang profile (Dùng sendRedirect để tránh lỗi resubmit form khi F5)
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}