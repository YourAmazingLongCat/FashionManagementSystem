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
 * Admin-only Comment Management page
 * URL: /comment-management
 */
@WebServlet("/comment-management")
public class CommentManagementServlet extends HttpServlet {

    private CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Auth check
        Account account = getAdminAccount(req, resp);
        if (account == null) return;

        String view = req.getParameter("view"); // "add", "edit", "delete" sub-pages
        String commentId = req.getParameter("commentId");

        if ("edit".equals(view) && commentId != null) {
            // Show edit form page
            Comment comment = commentDAO.getCommentById(commentId);
            if (comment == null) {
                resp.sendRedirect(req.getContextPath() + "/comment-management?msg=not_found");
                return;
            }
            req.setAttribute("comment", comment);
            req.getRequestDispatcher("/view/page/commentManagement/updateComment.jsp").forward(req, resp);

        } else if ("delete".equals(view) && commentId != null) {
            // Show delete confirmation page
            Comment comment = commentDAO.getCommentById(commentId);
            if (comment == null) {
                resp.sendRedirect(req.getContextPath() + "/comment-management?msg=not_found");
                return;
            }
            req.setAttribute("comment", comment);
            req.getRequestDispatcher("/view/page/commentManagement/deleteComment.jsp").forward(req, resp);

        } else if ("add".equals(view)) {
            // Show add comment form (admin manual add)
            req.getRequestDispatcher("/view/page/commentManagement/addComment.jsp").forward(req, resp);

        } else {
            // Default: list all comments
            List<Comment> comments = commentDAO.getAllComments();
            req.setAttribute("comments", comments);
            req.getRequestDispatcher("/view/page/commentManagement/listComment.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        Account account = getAdminAccount(req, resp);
        if (account == null) return;

        String action = req.getParameter("action");

        switch (action != null ? action : "") {

            case "update": {
                String commentId = req.getParameter("commentId");
                String ratingStr = req.getParameter("rating");
                String content = req.getParameter("content");
                try {
                    int rating = Integer.parseInt(ratingStr);
                    boolean success = commentDAO.updateComment(commentId, rating, content.trim());
                    resp.sendRedirect(req.getContextPath() + "/comment-management?msg=" + (success ? "updated" : "error"));
                } catch (Exception e) {
                    resp.sendRedirect(req.getContextPath() + "/comment-management?msg=error");
                }
                break;
            }

            case "delete": {
                String commentId = req.getParameter("commentId");
                boolean success = commentDAO.deleteComment(commentId);
                resp.sendRedirect(req.getContextPath() + "/comment-management?msg=" + (success ? "deleted" : "error"));
                break;
            }

            case "toggle": {
                String commentId = req.getParameter("commentId");
                boolean success = commentDAO.toggleCommentStatus(commentId);
                resp.sendRedirect(req.getContextPath() + "/comment-management?msg=" + (success ? "toggled" : "error"));
                break;
            }

            default:
                resp.sendRedirect(req.getContextPath() + "/comment-management");
        }
    }

    private Account getAdminAccount(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        Account account = (session != null) ? (Account) session.getAttribute("USER") : null;
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return null;
        }
        if (!isAdmin(account)) {
            resp.sendRedirect(req.getContextPath() + "/home?msg=no_permission");
            return null;
        }
        return account;
    }

    private boolean isAdmin(Account account) {
        return account != null && "Admin".equalsIgnoreCase(account.getRole());
    }
}
