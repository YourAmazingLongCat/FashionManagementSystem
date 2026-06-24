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

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderId = request.getParameter("orderId");

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }

        Order order = orderService.viewOrderDetailForStaff(orderId);

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            request.getRequestDispatcher("/Pages/Staff/orderDetail.jsp").forward(request, response);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForStaff(orderId);

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.getRequestDispatcher("/Pages/Staff/orderDetail.jsp").forward(request, response);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
