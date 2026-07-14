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
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;

@WebServlet(name = "CustomerOrderHistoryServlet", urlPatterns = {"/customer/order-history"})
public class CustomerOrderHistoryServlet extends HttpServlet {

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String ORDER_HISTORY_PAGE = "/Pages/Customer/orderHistory.jsp";

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

        String keyword = trim(request.getParameter("keyword"));
        List<Order> listOrders = isEmpty(keyword)
                ? orderService.viewOrderHistory(customerId)
                : orderService.searchOrderHistory(customerId, keyword);

        request.setAttribute("listOrders", listOrders);
        request.setAttribute("keyword", keyword);
        forwardLayout(request, response, ORDER_HISTORY_PAGE);
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
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ignored) {
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
