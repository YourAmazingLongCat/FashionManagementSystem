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
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * AJAX Data Endpoint for Product Comments.
 * Returns JSON formatted comment list, rating summary, and review eligibility.
 * 
 * @author ngocpace191049-cmyk
 */
@WebServlet("/comment-data")
public class CommentDataServlet extends HttpServlet {

    private final CommentDAO commentDAO = new CommentDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");

        String productId = req.getParameter("productId");
        String orderItemId = req.getParameter("orderItemId");
        String action = req.getParameter("action");

        HttpSession session = req.getSession(false);
        Account account = (session != null) ? (Account) session.getAttribute("USER") : null;

        // Check eligibility for specific order item (called from order detail page)
        if ("checkOrderItem".equals(action) && orderItemId != null && !orderItemId.isBlank()) {
            handleCheckOrderItemEligibility(resp, account, orderItemId);
            return;
        }

        if (productId == null || productId.isBlank()) {
            resp.getWriter().write("{\"error\":\"Missing productId\",\"comments\":[],\"eligibleOrderItemId\":null}");
            return;
        }

        // Staff/Admin sees both Active & Hidden comments; Guests & Customers see only Active comments
        List<Comment> comments;
        if (isStaffOrAdmin(account)) {
            comments = commentDAO.getAllCommentsByProduct(productId);
        } else {
            comments = commentDAO.getActiveCommentsByProduct(productId);
        }

        String eligibleOrderItemId = null;
        int remainingDaysToComment = -1;
        if (isCustomer(account)) {
            eligibleOrderItemId = commentDAO.getEligibleOrderItemId(account.getAccountId(), productId);
            remainingDaysToComment = commentDAO.getRemainingDaysToComment(account.getAccountId(), productId);
        }

        long editLimitMs = CommentDAO.EDIT_LIMIT_MS;
        long now = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"eligibleOrderItemId\":");
        if (eligibleOrderItemId != null) {
            sb.append("\"").append(jsonEscape(eligibleOrderItemId)).append("\"");
        } else {
            sb.append("null");
        }
        sb.append(",\"remainingDaysToComment\":").append(remainingDaysToComment);

        sb.append(",\"comments\":[");
        for (int i = 0; i < comments.size(); i++) {
            Comment c = comments.get(i);
            if (i > 0) sb.append(",");

            boolean canEdit = false;
            long daysLeft = 0;
            if (c.getCreatedAt() != null) {
                long diff = now - c.getCreatedAt().getTime();
                canEdit = diff <= editLimitMs;
                long totalDaysPassed = diff / (1000 * 60 * 60 * 24);
                daysLeft = Math.max(0, 7 - totalDaysPassed);
            }

            sb.append("{");
            sb.append("\"commentId\":\"").append(jsonEscape(c.getCommentId())).append("\",");
            sb.append("\"accountId\":\"").append(jsonEscape(c.getAccountId())).append("\",");
            sb.append("\"accountFullName\":\"").append(jsonEscape(c.getAccountFullName())).append("\",");
            sb.append("\"accountUsername\":\"").append(jsonEscape(c.getAccountUsername())).append("\",");
            sb.append("\"rating\":").append(c.getRating()).append(",");
            sb.append("\"content\":\"").append(jsonEscape(c.getContent())).append("\",");
            sb.append("\"createdAt\":\"").append(c.getCreatedAt() != null ? sdf.format(c.getCreatedAt()) : "").append("\",");
            sb.append("\"status\":\"").append(jsonEscape(c.getStatus())).append("\",");
            sb.append("\"variantInfo\":\"").append(jsonEscape(c.getVariantInfo())).append("\",");
            sb.append("\"canEdit\":").append(canEdit).append(",");
            sb.append("\"daysLeft\":").append(daysLeft);
            sb.append("}");
        }
        sb.append("]}");

        PrintWriter out = resp.getWriter();
        out.print(sb.toString());
        out.flush();
    }

    private void handleCheckOrderItemEligibility(HttpServletResponse resp,
            Account account, String orderItemId) throws IOException {
        if (account == null) {
            resp.getWriter().write("{\"error\":\"Please login\",\"eligible\":false,\"reason\":\"Please login to review\"}");
            return;
        }

        CommentDAO.EligibilityStatus status = commentDAO.checkOrderItemEligibility(orderItemId, account.getAccountId());

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"eligible\":").append(status.isEligible());
        sb.append(",\"alreadyReviewed\":").append(status.isAlreadyReviewed());
        sb.append(",\"windowExpired\":").append(status.isWindowExpired());
        sb.append(",\"remainingDays\":").append(status.getRemainingDays());
        sb.append(",\"reason\":\"").append(jsonEscape(status.getReason() != null ? status.getReason() : "")).append("\"");
        sb.append("}");

        resp.getWriter().write(sb.toString());
    }

    private boolean isStaffOrAdmin(Account account) {
        if (account == null) return false;
        String role = account.getRole();
        return "Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
    }

    private boolean isCustomer(Account account) {
        return account != null && "Customer".equalsIgnoreCase(account.getRole());
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}