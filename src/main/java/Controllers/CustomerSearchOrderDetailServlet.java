package Controllers;

import Models.Order;
import Services.OrderService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CustomerSearchOrderDetailServlet", urlPatterns = {"/customer/search-order-detail"})
public class CustomerSearchOrderDetailServlet extends HttpServlet {

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

        HttpSession session = request.getSession();
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/Pages/Authentication/login.jsp");
            return;
        }

        String orderId = request.getParameter("orderId");
        if (isEmpty(orderId)) {
            orderId = request.getParameter("keyword");
        }

        if (isEmpty(orderId)) {
            request.setAttribute("errorMessage", "Please enter an order ID.");
            request.getRequestDispatcher("/Pages/Customer/orderHistory.jsp").forward(request, response);
            return;
        }

        Order order = orderService.searchOrderDetailForCustomer(customerId, orderId);

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            request.setAttribute("keyword", orderId.trim());
            request.getRequestDispatcher("/Pages/Customer/orderHistory.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + order.getOrderId());
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
