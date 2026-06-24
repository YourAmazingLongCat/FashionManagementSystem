package Controllers;

import Models.Order;
import Models.OrderItem;
import Services.OrderService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StaffOrderDetailServlet", urlPatterns = {"/staff/order-detail"})
public class StaffOrderDetailServlet extends HttpServlet {

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String STAFF_ORDER_DETAIL_PAGE = "/Pages/Staff/orderDetail.jsp";

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderId = trim(request.getParameter("orderId"));

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }

        Order order = orderService.viewOrderDetailForStaff(orderId);
        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            forwardLayout(request, response, STAFF_ORDER_DETAIL_PAGE);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForStaff(orderId);
        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        forwardLayout(request, response, STAFF_ORDER_DETAIL_PAGE);
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
