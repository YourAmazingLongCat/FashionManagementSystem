import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

import DALs.AccountDAO;
import Models.Account;
import Utils.passwordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ChangePasswordController", urlPatterns = {"/change-password"})
public class ChangePasswordController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Kiểm tra xem user đã đăng nhập chưa (Bảo mật)
        HttpSession session = request.getSession();
        if (session.getAttribute("USER") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        // Trỏ về đúng vị trí file ChangePassword.jsp
        request.getRequestDispatcher("/Pages/Customer/ChangePassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Lấy thông tin user hiện tại từ Session
        HttpSession session = request.getSession();
        Account currentUser = (Account) session.getAttribute("USER"); 
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Lấy dữ liệu người dùng nhập từ form HTML
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            // 3. Kiểm tra mật khẩu mới và xác nhận có giống nhau không
            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
                request.getRequestDispatcher("/Pages/Customer/ChangePassword.jsp").forward(request, response);
                return;
            }

            // 4. Gọi DAO để lấy mật khẩu chuẩn từ Database lên đối chiếu
            AccountDAO accountDAO = new AccountDAO();
            Account accountInDb = accountDAO.getAccountById(currentUser.getAccountId());
            
            // 5. ĐÃ SỬA LẠI THÀNH getPassword() ĐỂ KHÔNG BỊ LỖI
            if (accountInDb == null || accountInDb.getPassword() == null
                    || !BCrypt.checkpw(oldPassword, accountInDb.getPassword())) {
                request.setAttribute("error", "Mật khẩu hiện tại không đúng!");
                request.getRequestDispatcher("/Pages/Customer/ChangePassword.jsp").forward(request, response);
                return;
            }

            // 6. Thực hiện update mật khẩu mới vào cơ sở dữ liệu
            boolean isUpdated = accountDAO.updatePassword(currentUser.getAccountId(), passwordUtil.hashPassword(newPassword));
            
            if (isUpdated) {
                // Đổi mật khẩu thành công
                request.setAttribute("success", "Đổi mật khẩu thành công!");
                
                // Cập nhật lại mật khẩu mới vào biến Session để đồng bộ
                // ĐÃ SỬA LẠI THÀNH setPassword() ĐỂ KHÔNG BỊ LỖI
                currentUser.setPassword(newPassword);
                session.setAttribute("USER", currentUser);
            } else {
                // Lỗi thực thi SQL
                request.setAttribute("error", "Có lỗi xảy ra khi cập nhật vào cơ sở dữ liệu!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }

        // 7. Dù cập nhật thành công hay thất bại, đều trả về lại trang JSP để hiển thị thông báo
        request.getRequestDispatcher("/Pages/Customer/ChangePassword.jsp").forward(request, response);
    }
}