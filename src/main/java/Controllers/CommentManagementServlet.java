package Controllers;

import DALs.CommentDAO;
import Models.Account;
import Models.Comment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller for Admin/Staff Comment Management:
 * - View all comments
 * - Search comments by keyword
 * - Hide / Unhide comments (status toggle)
 * - Update / Delete comments
 * 
 * @author ngocpace191049-cmyk
 */
@WebServlet("/comment-management")
public class CommentManagementServlet extends HttpServlet {

    private final CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account account = getAdminAccount(req, resp);
        if (account == null) return;

        String searchKeyword = req.getParameter("search");
        List<Comment> comments;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            comments = commentDAO.searchComments(searchKeyword.trim());
            req.setAttribute("search", searchKeyword.trim());
        } else {
            comments = commentDAO.getAllComments();
        }

        req.setAttribute("comments", comments);
        req.getRequestDispatcher("/Pages/Admin/Admin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        Account account = getAdminAccount(req, resp);
        if (account == null) return;

        String action = req.getParameter("action");
        String commentId = req.getParameter("commentId");

        switch (action != null ? action : "") {
            case "toggle":
                if (commentId != null) {
                    commentDAO.toggleCommentStatus(commentId);
                }
                break;
            case "delete":
                if (commentId != null) {
                    commentDAO.deleteComment(commentId);
                }
                break;
            case "update":
                String ratingStr = req.getParameter("rating");
                String content = req.getParameter("content");
                if (commentId != null && ratingStr != null && content != null) {
                    try {
                        int rating = Integer.parseInt(ratingStr);
                        commentDAO.updateComment(commentId, rating, content.trim());
                    } catch (NumberFormatException ignored) {}
                }
                break;
            default:
                break;
        }

        resp.sendRedirect(req.getContextPath() + "/comment-management");
    }

    private Account getAdminAccount(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        Account account = (session != null) ? (Account) session.getAttribute("USER") : null;
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return null;
        }
        if (!isAdminOrStaff(account)) {
            resp.sendRedirect(req.getContextPath() + "/home?msg=no_permission");
            return null;
        }
        return account;
    }

    private boolean isAdminOrStaff(Account account) {
        if (account == null) return false;
        String role = account.getRole();
        return "Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
    }
}
