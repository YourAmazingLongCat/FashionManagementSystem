package Controllers;

import DALs.AccountDAO;
import DALs.BillDAO;
import Models.Account;
import Models.Bill;
import Models.Order;
import Models.OrderItem;
import Services.OrderService;
import Services.PaymentService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StaffOrderDetailServlet", urlPatterns = {"/staff/order-detail"})
public class StaffOrderDetailServlet extends HttpServlet {

    private OrderService orderService;
    private PaymentService paymentService;
    private BillDAO billDAO;
    private AccountDAO accountDAO;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        paymentService = new PaymentService();
        billDAO = new BillDAO();
        accountDAO = new AccountDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");

        if (user == null || !isStaffOrAdmin(user)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        boolean modalRequest = "1".equals(request.getParameter("modal")) || isAjax(request);

        if (isEmpty(orderId)) {
            if (modalRequest) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("errorMessage", "Missing order ID.");
                forwardModal(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/staff/orders");
            }
            return;
        }

        // Staff Order Detail is popup-only. Direct links are normalized back to Manage Orders.
        if (!modalRequest) {
            response.sendRedirect(request.getContextPath()
                    + "/staff/orders?openOrder="
                    + URLEncoder.encode(orderId, StandardCharsets.UTF_8));
            return;
        }

        Order order = orderService.viewOrderDetailForStaff(orderId);
        if (order == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("errorMessage", "Order not found.");
            forwardModal(request, response);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForStaff(orderId);
        Bill bill = billDAO.getBillByOrderId(orderId);
        Models.Payment payment = paymentService.getPaymentByOrderId(orderId);
        Account customer = accountDAO.getAccountById(order.getCustomerId());

        request.setAttribute("order", order);
        request.setAttribute("customer", customer);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("bill", bill);
        request.setAttribute("payment", payment);
        forwardModal(request, response);
    }

    private void forwardModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        request.getRequestDispatcher("/Pages/Staff/orderDetailModal.jsp").forward(request, response);
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private boolean isStaffOrAdmin(Account user) {
        return "Staff".equalsIgnoreCase(user.getRole())
                || "Admin".equalsIgnoreCase(user.getRole());
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
