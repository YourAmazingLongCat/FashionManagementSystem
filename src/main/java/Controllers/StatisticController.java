/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controllers;

import java.io.IOException;
import java.util.List;

import DALs.AccountDAO;
import DALs.CommentDAO;
import DALs.StatisticDAO;
import Models.Account;
import Models.Comment;
import Utils.EmailUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/Admin", "/admin"})
public class StatisticController extends HttpServlet {

    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }
        Account currentUser = (Account) session.getAttribute("USER");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) {
            return;
        }

        loadDashboard(request, null);
        request.getRequestDispatcher("/Pages/Admin/Admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) {
            return;
        }

        AccountDAO accountDao = new AccountDAO();
        String action = request.getParameter("action");
        String accountId = request.getParameter("accountId");

        if ("updateRole".equals(action)) {
            String newRole = request.getParameter("role");
            if (accountId != null && !accountId.isEmpty() && newRole != null && !newRole.isEmpty()) {
                accountDao.updateRole(accountId, newRole);
                request.setAttribute("toastMsg", "Role updated successfully!");
            }
        } else if ("updateStatus".equals(action)) {
            String newStatus = request.getParameter("status");
            if (accountId != null && !accountId.isEmpty() && newStatus != null && !newStatus.isEmpty()) {
                accountDao.updateStatus(accountId, newStatus);
                request.setAttribute("toastMsg", "Status updated successfully!");
            }
        } else         if ("createStaff".equals(action)) {
            String email = request.getParameter("email");
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");

            if (email != null && !email.isBlank() && fullName != null && !fullName.isBlank()) {
                if (accountDao.emailExists(email)) {
                    request.setAttribute("toastErr", "Email already exists!");
                } else if (phone != null && !phone.isBlank() && accountDao.phoneExists(phone)) {
                    request.setAttribute("toastErr", "Phone number already exists!");
                } else {
                    // Generate random password
                    String generatedPassword = generateRandomPassword();
                    
                    Account newAcc = new Account();
                    newAcc.setAccountId(accountDao.generateNextAccountId());
                    newAcc.setEmail(email);
                    newAcc.setFullName(fullName);
                    newAcc.setRole("Staff");
                    newAcc.setPhone(phone != null && !phone.isBlank() ? phone : null);
                    newAcc.setStatus("Active");
                    newAcc.setUsername(email);

                    boolean success = accountDao.createAccount(newAcc, generatedPassword);
                    if (success) {
                        // Send email with credentials
                        boolean emailSent = EmailUtils.sendStaffCredentials(email, fullName, email, generatedPassword);
                        if (emailSent) {
                            request.setAttribute("toastMsg", "Staff account created and credentials sent to email!");
                        } else {
                            request.setAttribute("toastMsg", "Staff account created, but failed to send email!");
                        }
                    } else {
                        request.setAttribute("toastErr", "Failed to create staff account. Please try again!");
                    }
                }
            } else {
                request.setAttribute("toastErr", "Please fill in all required fields!");
            }
        } else if ("deleteAccount".equals(action)) {
            if (accountId != null && !accountId.isEmpty()) {
                HttpSession session = request.getSession(false);
                Account currentUser = (Account) session.getAttribute("USER");
                if (currentUser != null && !currentUser.getAccountId().equals(accountId)) {
                    accountDao.deleteAccount(accountId);
                    request.setAttribute("toastMsg", "Account deleted successfully!");
                } else {
                    request.setAttribute("toastErr", "You cannot delete your own account!");
                }
            }
        } else if ("toggleComment".equals(action)) {
            String commentId = request.getParameter("commentId");
            if (commentId != null && !commentId.isEmpty()) {
                CommentDAO commentDao = new CommentDAO();
                boolean success = commentDao.toggleCommentStatus(commentId);
                request.setAttribute("toastMsg", success ? "Comment visibility toggled!" : "Failed to toggle comment.");
            }
        } else if ("deleteComment".equals(action)) {
            String commentId = request.getParameter("commentId");
            if (commentId != null && !commentId.isEmpty()) {
                CommentDAO commentDao = new CommentDAO();
                boolean success = commentDao.deleteComment(commentId);
                request.setAttribute("toastMsg", success ? "Comment deleted!" : "Failed to delete comment.");
            }
        }

        String searchKeyword = request.getParameter("searchAccount");
        loadDashboard(request, searchKeyword);

        String quantityParam = request.getParameter("quantity");
        if (quantityParam != null && !quantityParam.isEmpty()) {
            StatisticDAO dao = new StatisticDAO();
            int quantity = Integer.parseInt(quantityParam);
            request.setAttribute("customerStatistics", dao.searchCustomerByOrderQuantity(quantity));
        }

        request.getRequestDispatcher("/Pages/Admin/Admin.jsp").forward(request, response);
    }

    private void loadDashboard(HttpServletRequest request, String searchKeyword) {
        StatisticDAO dao = new StatisticDAO();
        AccountDAO accountDao = new AccountDAO();
        CommentDAO commentDao = new CommentDAO();

        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String currentSection = request.getParameter("section");
        String searchComment = request.getParameter("searchComment");

        // Load comment data if comments section is active
        if ("comments".equals(currentSection)) {
            List<Comment> allComments = commentDao.getAllComments();

            // Filter by date range
            if ((fromDate != null && !fromDate.isEmpty()) || (toDate != null && !toDate.isEmpty())) {
                final String fFrom = fromDate;
                final String fTo = toDate;
                allComments = allComments.stream()
                    .filter(c -> {
                        if (c.getCreatedAt() == null) return false;
                        java.sql.Timestamp ts = c.getCreatedAt() instanceof java.sql.Timestamp
                            ? (java.sql.Timestamp) c.getCreatedAt()
                            : new java.sql.Timestamp(c.getCreatedAt().getTime());
                        if (fFrom != null && !fFrom.isEmpty()) {
                            java.sql.Date from = java.sql.Date.valueOf(fFrom);
                            if (ts.compareTo(from) < 0) return false;
                        }
                        if (fTo != null && !fTo.isEmpty()) {
                            java.sql.Date to = java.sql.Date.valueOf(fTo);
                            java.sql.Date nextDay = new java.sql.Date(to.getTime() + 86400000L);
                            if (ts.compareTo(nextDay) >= 0) return false;
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }

            // Filter by search keyword (product name, customer name, or comment content)
            List<Comment> filteredComments = allComments;
            if (searchComment != null && !searchComment.trim().isEmpty()) {
                String kw = searchComment.trim().toLowerCase();
                filteredComments = allComments.stream()
                    .filter(c -> (c.getProductName() != null && c.getProductName().toLowerCase().contains(kw))
                              || (c.getAccountFullName() != null && c.getAccountFullName().toLowerCase().contains(kw))
                              || (c.getAccountUsername() != null && c.getAccountUsername().toLowerCase().contains(kw))
                              || (c.getContent() != null && c.getContent().toLowerCase().contains(kw)))
                    .collect(java.util.stream.Collectors.toList());
            }
            request.setAttribute("allComments", filteredComments);
            request.setAttribute("totalComments", allComments.size());
            long activeCount = allComments.stream().filter(c -> "Active".equalsIgnoreCase(c.getStatus())).count();
            long hiddenCount = allComments.stream().filter(c -> "Hidden".equalsIgnoreCase(c.getStatus())).count();
            request.setAttribute("activeComments", activeCount);
            request.setAttribute("hiddenComments", hiddenCount);
        }

        var topCustomers = dao.getTopCustomers();
        var topSpenders = dao.getTopSpenders(10, fromDate, toDate);

        request.setAttribute("totalCustomers", dao.getTotalCustomers());
        request.setAttribute("totalOrders", dao.getTotalOrders());
        request.setAttribute("revenue", dao.getRevenue(fromDate, toDate));
        request.setAttribute("costOfGoodsSold", dao.getCostOfGoodsSold(fromDate, toDate));
        request.setAttribute("profit", dao.getProfit(fromDate, toDate));
        request.setAttribute("totalImportCost", dao.getTotalImportCost(fromDate, toDate));
        request.setAttribute("totalProductSold", dao.getTotalProductSold(fromDate, toDate));
        request.setAttribute("topProducts", dao.getTopProducts(5, fromDate, toDate));
        request.setAttribute("productSales", dao.getProductSales(fromDate, toDate));
        request.setAttribute("topSpenders", topSpenders);
        request.setAttribute("customerStatistics", topCustomers);
        request.setAttribute("orderStatistics", dao.getOrderStatistics());
        request.setAttribute("allAccounts", searchKeyword != null && !searchKeyword.trim().isEmpty()
                ? accountDao.searchAccounts(searchKeyword.trim())
                : accountDao.getAllAccounts());
        
        // Account pagination
        String pageParam = request.getParameter("page");
        int page = 1;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        int pageSize = 10;
        String searchAcc = request.getParameter("searchAccount");
        int totalAccounts = accountDao.getTotalAccounts(searchAcc);
        int totalPages = (int) Math.ceil((double) totalAccounts / pageSize);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;
        
        request.setAttribute("accountPage", page);
        request.setAttribute("accountPageSize", pageSize);
        request.setAttribute("accountTotalPages", totalPages);
        request.setAttribute("accountTotalRecords", totalAccounts);
        request.setAttribute("pagedAccounts", accountDao.getAccountsPaged(page, pageSize, searchAcc));
    }

    private String generateRandomPassword() {
        // Format: 1 uppercase + 1 lowercase + 1 number + 1 special + 6 random (total 10 chars)
        String uppercase = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lowercase = "abcdefghjkmnpqrstuvwxyz";
        String numbers = "23456789";
        String special = "!@#$%&*";
        String allChars = uppercase + lowercase + numbers + special;
        
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each required type
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fill remaining 6 characters
        for (int i = 4; i < 10; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password characters
        char[] charsArr = password.toString().toCharArray();
        for (int i = charsArr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = charsArr[i];
            charsArr[i] = charsArr[j];
            charsArr[j] = tmp;
        }

        return new String(charsArr);
    }
}
