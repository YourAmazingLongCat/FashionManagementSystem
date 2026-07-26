package Controllers;

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
        
        request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
        request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
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

        // 3. Kiểm tra các trường rỗng
        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 4. Kiểm tra mật khẩu mới và xác nhận có giống nhau không
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 5. Kiểm tra mật khẩu mới không trùng với mật khẩu cũ
        if (newPassword.equals(oldPassword)) {
            request.setAttribute("error", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 6. Kiểm tra độ mạnh mật khẩu mới
        if (newPassword.length() < 8) {
            request.setAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            request.setAttribute("error", "Mật khẩu mới phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 7. Gọi DAO để lấy mật khẩu chuẩn từ Database lên đối chiếu
        AccountDAO accountDAO = new AccountDAO();
        Account accountInDb = accountDAO.getAccountById(currentUser.getAccountId());

        // 8. ĐÃ SỬA LẠI THÀNH getPassword() ĐỂ KHÔNG BỊ LỖI
        if (accountInDb == null || accountInDb.getPassword() == null
                || !BCrypt.checkpw(oldPassword, accountInDb.getPassword())) {
            request.setAttribute("error", "Mật khẩu hiện tại không đúng!");
            request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
            request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        // 9. Thực hiện update mật khẩu mới vào cơ sở dữ liệu
        String hashedPassword = passwordUtil.hashPassword(newPassword);
        boolean isUpdated = accountDAO.updatePassword(currentUser.getAccountId(), hashedPassword);
        
        if (isUpdated) {
            request.setAttribute("success", "Đổi mật khẩu thành công!");
            currentUser.setPassword(hashedPassword);
            session.setAttribute("USER", currentUser);
        } else {
            request.setAttribute("error", "Có lỗi xảy ra khi cập nhật vào cơ sở dữ liệu!");
        }

        // 7. Dù cập nhật thành công hay thất bại, đều trả về lại trang JSP để hiển thị thông báo
        request.setAttribute("categories", new DALs.CategoryDAO().getAllCategories());
        request.setAttribute("contentPage", "/Pages/Customer/ChangePassword.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }
}