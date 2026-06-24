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

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/Pages/Authentication/login.jsp");
            return;
        }

        String orderId = request.getParameter("orderId");

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        Order order = orderService.viewOrderDetailForCustomer(customerId, orderId);

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found or you do not have permission to view this order.");
            request.getRequestDispatcher("/Pages/Customer/orderDetail.jsp").forward(request, response);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForCustomer(customerId, orderId);

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.getRequestDispatcher("/Pages/Customer/orderDetail.jsp").forward(request, response);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
