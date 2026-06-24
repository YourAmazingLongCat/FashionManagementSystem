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
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CustomerOrderDetailServlet", urlPatterns = {"/customer/order-detail"})
public class CustomerOrderDetailServlet extends HttpServlet {

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String ORDER_DETAIL_PAGE = "/Pages/Customer/orderDetail.jsp";

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Missing order ID.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        Order order = orderService.viewOrderDetailForCustomer(customerId, orderId);
        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            forwardLayout(request, response, ORDER_DETAIL_PAGE);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForCustomer(customerId, orderId);
        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        forwardLayout(request, response, ORDER_DETAIL_PAGE);
    }

    private void forwardLayout(HttpServletRequest request, HttpServletResponse response, String contentPage)
            throws ServletException, IOException {
        request.setAttribute("contentPage", contentPage);
        request.getRequestDispatcher(LAYOUT_PAGE).forward(request, response);
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString();
        }

        Object user = session.getAttribute("USER");
        if (user == null) {
            return null;
        }

        String[] methodNames = {"getAccountId", "getCustomerId", "getUserId", "getId"};
        for (String methodName : methodNames) {
            try {
                Object value = user.getClass().getMethod(methodName).invoke(user);
                if (value != null && !value.toString().trim().isEmpty()) {
                    return value.toString();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
