package Controllers;

import DALs.ProductDAO;
import Models.Account;
import Models.Order;
import Models.Payment;
import Models.Product;
import Services.OrderService;
import Services.PaymentService;
import Utils.OrderStatus;
import Utils.PaymentStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "StaffDashboardServlet", urlPatterns = {"/staff/dashboard"})
public class StaffDashboardServlet extends HttpServlet {

    private OrderService orderService;
    private PaymentService paymentService;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        paymentService = new PaymentService();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account currentUser = (Account) session.getAttribute("USER");

        if (currentUser == null || !isStaffOrAdmin(currentUser)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<Order> orders = safeOrders();
        List<Payment> payments = safePayments();
        List<Payment> pendingDeposits = safePendingDeposits();
        List<Product> products = safeProducts();

        request.setAttribute("totalOrders", orders.size());
        request.setAttribute("pendingOrders", countOrdersByStatus(orders, OrderStatus.PENDING));
        request.setAttribute("processingOrders", countOrdersByStatus(orders, OrderStatus.PROCESSING));
        request.setAttribute("shippingOrders", countOrdersByStatus(orders, OrderStatus.SHIPPING));
        request.setAttribute("totalPayments", payments.size());
        request.setAttribute("pendingPayments", countPaymentsByStatus(payments, PaymentStatus.PENDING));
        request.setAttribute("paidPayments", countPaymentsByStatus(payments, PaymentStatus.PAID));
        request.setAttribute("pendingDepositsCount", pendingDeposits.size());
        request.setAttribute("totalProducts", products.size());
        request.setAttribute("recentOrders", orders);
        request.setAttribute("payments", payments);
        request.setAttribute("pendingDeposits", pendingDeposits);

        request.getRequestDispatcher("/Pages/Staff/Staff.jsp").forward(request, response);
    }

    private List<Order> safeOrders() {
        List<Order> orders = orderService.viewOrdersForStaff();
        return orders == null ? new ArrayList<>() : orders;
    }

    private List<Payment> safePayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return payments == null ? new ArrayList<>() : payments;
    }

    private List<Payment> safePendingDeposits() {
        List<Payment> pending = paymentService.getPendingDeposits();
        return pending == null ? new ArrayList<>() : pending;
    }

    private List<Product> safeProducts() {
        List<Product> products = productDAO.getAllProducts();
        return products == null ? new ArrayList<>() : products;
    }

    private int countOrdersByStatus(List<Order> orders, String status) {
        int count = 0;
        for (Order order : orders) {
            if (order != null && status.equalsIgnoreCase(order.getOrderStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countPaymentsByStatus(List<Payment> payments, String status) {
        int count = 0;
        for (Payment payment : payments) {
            if (payment != null && status.equalsIgnoreCase(payment.getPaymentStatus())) {
                count++;
            }
        }
        return count;
    }

    private boolean isStaffOrAdmin(Account user) {
        return user.getRole() != null
                && (user.getRole().equalsIgnoreCase("Staff") || user.getRole().equalsIgnoreCase("Admin"));
    }
}
