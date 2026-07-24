package Controllers;

import Models.Order;
import Services.OrderService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StaffOrderServlet", urlPatterns = {"/staff/orders"})
public class StaffOrderServlet extends HttpServlet {

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String STAFF_ORDERS_PAGE = "/Pages/Staff/orders.jsp";

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = trim(request.getParameter("keyword"));

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

        int totalOrders = orderService.countSearchOrdersForStaff(keyword);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        List<Order> listOrders = orderService.searchOrdersForStaff(keyword, page, pageSize);

        request.setAttribute("listOrders", listOrders);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("contentPage", STAFF_ORDERS_PAGE);
        request.setAttribute("hideStaffHeader", "true");
        request.getRequestDispatcher(LAYOUT_PAGE).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
