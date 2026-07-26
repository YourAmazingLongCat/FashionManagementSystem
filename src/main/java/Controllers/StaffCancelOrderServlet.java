package Controllers;

import Services.BillIntegrationService;
import Services.OrderService;
import Services.PaymentService;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StaffCancelOrderServlet", urlPatterns = {"/staff/cancel-order"})
public class StaffCancelOrderServlet extends HttpServlet {

    private OrderService orderService;
    private PaymentService paymentService;
    private BillIntegrationService billService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        paymentService = new PaymentService();
        billService = new BillIntegrationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/staff/orders");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String orderId = trim(request.getParameter("orderId"));

        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Missing order ID.");
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }

        boolean cancelled = orderService.cancelOrder(orderId);

        if (cancelled) {
            paymentService.refundPaymentIfNeeded(orderId);
            session.setAttribute("successMessage", "Order cancelled successfully. Payment has been refunded if applicable.");
        } else {
            session.setAttribute("errorMessage", "This order cannot be cancelled by staff before the customer presses Place order or after shipping begins.");
        }

        response.sendRedirect(request.getContextPath() + "/staff/order-detail?orderId=" + orderId);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
