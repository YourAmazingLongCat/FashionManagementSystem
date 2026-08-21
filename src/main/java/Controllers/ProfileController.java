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
 * Controller for viewing and updating user profile.
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile", "/profile/update"})
public class ProfileController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Check login
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");

        if (user == null) {
            // Not logged in: redirect to login
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Get categories for Header.jsp
        request.setAttribute("categories", new CategoryDAO().getAllCategories());
        
        // Đảm bảo đường dẫn này khớp với vị trí file Profile.jsp trong project của bạn
        request.setAttribute("contentPage", "/Pages/Customer/Profile.jsp");

        // 3. Forward to shared layout
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set UTF-8 for Vietnamese characters
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 1. Read form data
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        // 2. Validate
        AccountDAO dao = new AccountDAO();
        if (fullName == null || fullName.isBlank()) {
            // Đã đổi thành errorMessage cho khớp với Layout.jsp
            session.setAttribute("errorMessage", "Full name cannot be empty!");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        if (phone != null && !phone.isBlank() && dao.phoneExistsForOtherAccount(phone, user.getAccountId())) {
            session.setAttribute("errorMessage", "Phone number is already used by another account!");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        // 3. Update Account object
        user.setFullName(fullName);
        user.setPhone(phone);
        
        // Tránh tình trạng Staff/Admin bị mất địa chỉ cũ do form ẩn trường address
        if (address != null) {
            user.setAddress(address);
        }

        // 4. Call DAO to update database
        boolean isSuccess = dao.updateProfile(user);

        if (isSuccess) {
            // Refresh session so header updates immediately
            session.setAttribute("USER", user);
            // Đã đổi thành successMessage cho khớp với Layout.jsp
            session.setAttribute("successMessage", "Profile updated successfully!");
        } else {
            session.setAttribute("errorMessage", "Error while saving data!");
        }

        // 5. Redirect back to profile
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}