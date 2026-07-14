package Controllers;

import Models.Order;
import Models.OrderItem;
import Models.Payment;
import Services.OrderService;
import Services.PaymentService;
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
    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderId = request.getParameter("orderId");

        if (orderId == null || orderId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }

        Order order = orderService.viewOrderDetailForStaff(orderId.trim());

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            request.setAttribute("contentPage", "/Pages/Staff/orderDetail.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForStaff(orderId.trim());
        Payment payment = paymentService.getPaymentByOrderId(orderId.trim());

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("payment", payment);
        request.setAttribute("contentPage", "/Pages/Staff/orderDetail.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }
}
