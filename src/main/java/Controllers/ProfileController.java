/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import DALs.AccountDAO;
import Models.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

/**
 * Controller xử lý xem và cập nhật hồ sơ cá nhân
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile", "/profile/update"})
// BẮT BUỘC CÓ DÒNG NÀY ĐỂ XỬ LÝ UPLOAD FILE:
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
                 maxFileSize = 1024 * 1024 * 10,      // Tối đa 10MB cho 1 file
                 maxRequestSize = 1024 * 1024 * 50)   // Tối đa 50MB cho cả request
public class ProfileController extends HttpServlet {

    // THƯ MỤC LƯU ẢNH (Nằm trong thư mục web/assets/avatars của project)
    private static final String UPLOAD_DIR = "assets/avatars";

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

        // 2. Chuyển hướng sang giao diện Profile.jsp
        request.getRequestDispatcher("/Pages/Customer/Profile.jsp").forward(request, response);
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

        // 2. Xử lý Upload Avatar
        String avatarPath = user.getAvatar(); // Mặc định giữ nguyên ảnh cũ nếu người dùng ko up ảnh mới
        Part filePart = request.getPart("avatarFile");

        // Kiểm tra xem người dùng có chọn file không (kích thước > 0)
        if (filePart != null && filePart.getSize() > 0) {
            // Lấy đường dẫn tuyệt đối thư mục build trên server
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
            
            // Tạo thư mục nếu chưa tồn tại
            File uploadFolder = new File(uploadFilePath);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            // Lấy tên file gốc
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            // Đổi tên file để không bị trùng (Ví dụ: avatar_ACC001_168923456.jpg)
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String newFileName = "avatar_" + user.getAccountId() + "_" + System.currentTimeMillis() + extension;

            // Lưu file vào ổ cứng server
            filePart.write(uploadFilePath + File.separator + newFileName);
            
            // Đường dẫn lưu vào Database (đường dẫn tương đối)
            avatarPath = UPLOAD_DIR + "/" + newFileName;
        }

        // 3. Cập nhật dữ liệu vào object Account hiện tại
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAvatar(avatarPath);

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