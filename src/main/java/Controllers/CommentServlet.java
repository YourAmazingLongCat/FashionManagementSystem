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

/**
 * Controller handling Comment actions from Product and Order details:
 * - Create comment (add)
 * - Create review from delivered order (addFromOrder)
 * - Update review (update)
 * - Delete review (delete)
 * - Hide / Unhide review (toggle)
 * 
 * @author ngocpace191049-cmyk
 */
@WebServlet("/comment")
public class CommentServlet extends HttpServlet {

    private final CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        Account account = (session != null) ? (Account) session.getAttribute("USER") : null;

        String action = req.getParameter("action");
        String productId = req.getParameter("productId");
        String redirectUrl = req.getContextPath() + "/home";
        if (productId != null && !productId.isBlank()) {
            redirectUrl = req.getContextPath() + "/home/view-detail-product?productId=" + productId + "&openComments=true";
        }

        // Must be logged in to comment or manage comments
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        switch (action != null ? action : "") {
            case "add":
                handleAdd(req, resp, account, redirectUrl);
                break;
            case "addFromOrder":
                handleAddFromOrder(req, resp, account);
                break;
            case "update":
                handleUpdate(req, resp, account, redirectUrl);
                break;
            case "delete":
                handleDelete(req, resp, account, redirectUrl);
                break;
            case "toggle":
                handleToggle(req, resp, account, redirectUrl);
                break;
            default:
                resp.sendRedirect(redirectUrl);
        }
    }

    private void handleAdd(HttpServletRequest req, HttpServletResponse resp,
                           Account account, String redirectUrl) throws IOException {
        String productId = req.getParameter("productId");
        String ratingStr = req.getParameter("rating");
        String content = req.getParameter("content");

        if (productId == null || ratingStr == null || content == null || content.trim().isEmpty()) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=missing_fields");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=invalid_rating");
            return;
        }

        String eligibleVariantId = commentDAO.getEligibleOrderItemId(account.getAccountId(), productId);
        if (eligibleVariantId == null) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=not_purchased");
            return;
        }

        Comment comment = new Comment();
        comment.setOrderItemId(eligibleVariantId);
        comment.setAccountId(account.getAccountId());
        comment.setRating(rating);
        comment.setContent(content.trim());

        boolean success = commentDAO.addComment(comment);
        resp.sendRedirect(redirectUrl + (success ? "&msg=added" : "&msg=error&detail=db_error"));
    }

    private void handleAddFromOrder(HttpServletRequest req, HttpServletResponse resp, Account account) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String orderItemId = req.getParameter("orderItemId");
        String ratingStr = req.getParameter("rating");
        String content = req.getParameter("content");

        if (orderItemId == null || ratingStr == null) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Missing required fields\"}");
            return;
        }

        CommentDAO.EligibilityStatus eligibility = commentDAO.checkOrderItemEligibility(orderItemId, account.getAccountId());
        if (!eligibility.isEligible()) {
            String msg = eligibility.getReason() != null ? eligibility.getReason() : "Cannot review this product";
            resp.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid rating value\"}");
            return;
        }

        Comment comment = new Comment();
        comment.setOrderItemId(orderItemId);
        comment.setAccountId(account.getAccountId());
        comment.setRating(rating);
        comment.setContent(content != null ? content.trim() : "");

        boolean success = commentDAO.addComment(comment);
        if (success) {
            resp.getWriter().write("{\"success\":true,\"message\":\"Review submitted successfully!\"}");
        } else {
            resp.getWriter().write("{\"success\":false,\"message\":\"Failed to submit review.\"}");
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp,
                              Account account, String redirectUrl) throws IOException {
        if (isCustomer(account)) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=cannot_edit");
            return;
        }

        String commentId = req.getParameter("commentId");
        String ratingStr = req.getParameter("rating");
        String content = req.getParameter("content");

        if (commentId == null || ratingStr == null || content == null || content.trim().isEmpty()) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=missing_fields");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=invalid_rating");
            return;
        }

        boolean isOwner = commentDAO.isCommentOwner(commentId, account.getAccountId());
        if (!isOwner && !isStaffOrAdmin(account)) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=no_permission");
            return;
        }

        boolean success = commentDAO.updateComment(commentId, rating, content.trim());
        resp.sendRedirect(redirectUrl + (success ? "&msg=updated" : "&msg=error&detail=db_error"));
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp,
                              Account account, String redirectUrl) throws IOException {
        if (isCustomer(account)) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=cannot_delete");
            return;
        }

        String commentId = req.getParameter("commentId");
        if (commentId == null) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=missing_fields");
            return;
        }

        boolean isOwner = commentDAO.isCommentOwner(commentId, account.getAccountId());
        if (!isOwner && !isStaffOrAdmin(account)) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=no_permission");
            return;
        }

        boolean success = commentDAO.deleteComment(commentId);
        resp.sendRedirect(redirectUrl + (success ? "&msg=deleted" : "&msg=error&detail=db_error"));
    }

    private void handleToggle(HttpServletRequest req, HttpServletResponse resp,
                              Account account, String redirectUrl) throws IOException {
        if (!isStaffOrAdmin(account)) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=no_permission");
            return;
        }

        String commentId = req.getParameter("commentId");
        if (commentId == null) {
            resp.sendRedirect(redirectUrl + "&msg=error&detail=missing_fields");
            return;
        }

        boolean success = commentDAO.toggleCommentStatus(commentId);
        resp.sendRedirect(redirectUrl + (success ? "&msg=toggled" : "&msg=error&detail=db_error"));
    }

    private boolean isStaffOrAdmin(Account account) {
        if (account == null) return false;
        String role = account.getRole();
        return "Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
    }

    private boolean isCustomer(Account account) {
        return account != null && "Customer".equalsIgnoreCase(account.getRole());
    }
}