/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import java.io.IOException;
import java.util.List;

import DALs.AccountDAO;
import Models.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminController", urlPatterns = {"/admin"})
public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Kiểm tra bảo mật: Phải đăng nhập VÀ phải là Admin
        HttpSession session = request.getSession();
        Account currentUser = (Account) session.getAttribute("USER");
        
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("Admin")) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Gọi DAO lấy danh sách tài khoản và đẩy sang JSP
        AccountDAO dao = new AccountDAO();
        List<Account> accountList = dao.getAllAccounts();
        request.setAttribute("accountList", accountList);
        
        // 3. Chuyển hướng tới giao diện của Admin
        request.getRequestDispatcher("/Pages/Admin/Admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        // 1. Kiểm tra bảo mật khi nhận request POST
        HttpSession session = request.getSession();
        Account currentUser = (Account) session.getAttribute("USER");
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("Admin")) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Lấy hành động (action) và ID tài khoản từ form gửi lên
        String action = request.getParameter("action");
        String accountId = request.getParameter("accountId");
        AccountDAO dao = new AccountDAO();

        // 3. Xử lý các thao tác tương ứng
        if ("updateRole".equals(action)) {
            String newRole = request.getParameter("role");
            dao.updateRole(accountId, newRole);
            request.setAttribute("toastMsg", "Cập nhật quyền thành công!");
            
        } else if ("updateStatus".equals(action)) {
            String newStatus = request.getParameter("status");
            dao.updateStatus(accountId, newStatus);
            request.setAttribute("toastMsg", "Cập nhật trạng thái thành công!");
        }

        // 4. Xử lý xong thì gọi lại doGet để load lại bảng danh sách mới nhất
        doGet(request, response);
    }
}