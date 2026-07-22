/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controllers;

import java.io.IOException;

import DALs.AccountDAO;
import DALs.StatisticDAO;
import Models.Account;
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
        } else if ("createAccount".equals(action)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String fullName = request.getParameter("fullName");
            String role = request.getParameter("role");
            String phone = request.getParameter("phone");

            if (email != null && !email.isEmpty() && password != null && !password.isEmpty()
                    && fullName != null && !fullName.isEmpty() && role != null && !role.isEmpty()) {
                String passwordPattern = "^[A-Z](?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{5,}$";
                if (!password.matches(passwordPattern)) {
                    request.setAttribute("toastErr", "Password must start with uppercase, contain at least 1 number, 1 special character, and be at least 6 characters!");
                } else if (accountDao.emailExists(email)) {
                    request.setAttribute("toastErr", "Email already exists!");
                } else {
                    Account newAcc = new Account();
                    newAcc.setAccountId(accountDao.generateNextAccountId());
                    newAcc.setEmail(email);
                    newAcc.setFullName(fullName);
                    newAcc.setRole(role);
                    newAcc.setPhone(phone != null ? phone : "");
                    newAcc.setStatus("Active");
                    
                    boolean success = accountDao.createAccount(newAcc, password);
                    if (success) {
                        request.setAttribute("toastMsg", "Account created successfully!");
                    } else {
                        request.setAttribute("toastErr", "Failed to create account. Please try again!");
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

        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");

        var topCustomers = dao.getTopCustomers();
        var topSpenders = dao.getTopSpenders(10, fromDate, toDate);

        System.out.println("[DEBUG] loadDashboard: totalCustomers=" + dao.getTotalCustomers());
        System.out.println("[DEBUG] loadDashboard: totalOrders=" + dao.getTotalOrders());
        System.out.println("[DEBUG] loadDashboard: topCustomers size=" + topCustomers.size());
        System.out.println("[DEBUG] loadDashboard: topSpenders size=" + topSpenders.size());

        request.setAttribute("totalCustomers", dao.getTotalCustomers());
        request.setAttribute("totalOrders", dao.getTotalOrders());
        request.setAttribute("revenue", dao.getRevenue(fromDate, toDate));
        request.setAttribute("profit", dao.getProfit(fromDate, toDate));
        request.setAttribute("costOfGoodsSold", dao.getCostOfGoodsSold(fromDate, toDate));
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
}
