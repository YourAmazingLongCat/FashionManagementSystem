package Controllers;

import Models.Account;
import Models.Order;
import Services.OrderService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StaffOrderServlet", urlPatterns = {"/staff/orders"})
public class StaffOrderServlet extends HttpServlet {

    private static final String STAFF_ORDERS_PAGE = "/Pages/Staff/orders.jsp";

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account user = session == null ? null : (Account) session.getAttribute("USER");
        if (user == null || !isStaffOrAdmin(user)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String keyword = trim(request.getParameter("keyword"));
        String status = trim(request.getParameter("status"));
        LocalDateTime dateFrom = parseDateStart(request.getParameter("dateFrom"));
        LocalDateTime dateTo = parseDateEnd(request.getParameter("dateTo"));

        int page = 1;
        int pageSize = 10;
        try {
            if (request.getParameter("page") != null) {
                page = Math.max(1, Integer.parseInt(request.getParameter("page")));
            }
            if (request.getParameter("pageSize") != null) {
                pageSize = Math.max(5, Math.min(50, Integer.parseInt(request.getParameter("pageSize"))));
            }
        } catch (NumberFormatException ignored) {}

        int totalOrders = orderService.countSearchOrdersForStaff(keyword, status, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        List<Order> listOrders = orderService.searchOrdersForStaff(keyword, status, dateFrom, dateTo, page, pageSize);

        request.setAttribute("listOrders", listOrders);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("dateFrom", request.getParameter("dateFrom"));
        request.setAttribute("dateTo", request.getParameter("dateTo"));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalOrders", totalOrders);
        request.getRequestDispatcher(STAFF_ORDERS_PAGE).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isStaffOrAdmin(Account user) {
        return "Staff".equalsIgnoreCase(user.getRole())
                || "Admin".equalsIgnoreCase(user.getRole());
    }

    private LocalDateTime parseDateStart(String value) {
        if (isEmpty(value)) return null;
        try {
            return LocalDate.parse(value.trim()).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateEnd(String value) {
        if (isEmpty(value)) return null;
        try {
            return LocalDate.parse(value.trim()).atTime(LocalTime.MAX);
        } catch (Exception e) {
            return null;
        }
    }
}
