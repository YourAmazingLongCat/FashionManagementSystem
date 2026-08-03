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

        // 1. Get current session (false = don't create new session if none)
        HttpSession session = request.getSession(false);

        // 2. Invalidate session if it exists
        if (session != null) {
            session.invalidate();
        }

        // 3. Redirect to home page
        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If logout form uses POST, just call doGet for simplicity
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Logout Controller - Fashion Management System";
    }
}
