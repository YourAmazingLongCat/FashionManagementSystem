package Controllers;

import Models.Order;
import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StaffSearchOrderDetailServlet", urlPatterns = {"/staff/search-order-detail"})
public class StaffSearchOrderDetailServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String orderId = request.getParameter("orderId");
        if (isEmpty(orderId)) {
            orderId = request.getParameter("keyword");
        }

        if (isEmpty(orderId)) {
            request.setAttribute("errorMessage", "Please enter an order ID.");
            request.getRequestDispatcher("/Pages/Staff/orders.jsp").forward(request, response);
            return;
        }

        Order order = orderService.searchOrderDetailForStaff(orderId);

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            request.setAttribute("keyword", orderId.trim());
            request.getRequestDispatcher("/Pages/Staff/orders.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/staff/order-detail?orderId=" + order.getOrderId());
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
