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

@WebServlet("/comment-data")
public class CommentDataServlet extends HttpServlet {

    private CommentDAO commentDAO = new CommentDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");

        String productId = req.getParameter("productId");
        if (productId == null || productId.trim().isEmpty()) {
            resp.getWriter().write("{\"error\":\"Missing productId\",\"comments\":[],\"eligibleOrderItemId\":null}");
            return;
        }

        HttpSession session = req.getSession(false);
        Account account = (session != null) ? (Account) session.getAttribute("USER") : null;

        // Admin/Staff thấy tất cả kể cả ẩn, người khác chỉ thấy Active
        List<Comment> comments;
        if (isStaffOrAdmin(account)) {
            comments = commentDAO.getAllCommentsByProduct(productId);
        } else {
            comments = commentDAO.getActiveCommentsByProduct(productId);
        }

        // Customer: kiểm tra có đơn hàng đủ điều kiện comment không
        String eligibleOrderItemId = null;
        if (isCustomer(account)) {
            eligibleOrderItemId = commentDAO.getEligibleOrderItemId(account.getAccountId(), productId);
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

        sb.append(",\"comments\":[");
        for (int i = 0; i < comments.size(); i++) {
            Comment c = comments.get(i);
            if (i > 0) sb.append(",");

            // Tính canEdit: còn trong 7 ngày không
            boolean canEdit = false;
            if (c.getCreatedAt() != null) {
                long diff = now - c.getCreatedAt().getTime();
                canEdit = diff <= editLimitMs;
            }

            // Tính số ngày còn lại để edit
            long daysLeft = 0;
            if (canEdit && c.getCreatedAt() != null) {
                long diff = now - c.getCreatedAt().getTime();
                daysLeft = 1 - (diff / (1000 * 60));
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

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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