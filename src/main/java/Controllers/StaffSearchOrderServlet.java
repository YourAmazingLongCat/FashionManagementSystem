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

@WebServlet(name = "StaffSearchOrderServlet", urlPatterns = {"/staff/search-orders"})
public class StaffSearchOrderServlet extends HttpServlet {

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
        List<Order> listOrders = isEmpty(keyword)
                ? orderService.viewOrdersForStaff()
                : orderService.searchOrdersForStaff(keyword);

        request.setAttribute("listOrders", listOrders);
        request.setAttribute("keyword", keyword);
        forwardLayout(request, response, STAFF_ORDERS_PAGE);
    }

    private void forwardLayout(HttpServletRequest request, HttpServletResponse response, String contentPage)
            throws ServletException, IOException {
        request.setAttribute("contentPage", contentPage);
        request.getRequestDispatcher(LAYOUT_PAGE).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
