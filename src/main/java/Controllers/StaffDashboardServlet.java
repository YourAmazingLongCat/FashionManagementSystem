package Controllers;

import DALs.ProductDAO;
import DALs.StatisticDAO;
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
    private StatisticDAO statisticDAO;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        paymentService = new PaymentService();
        productDAO = new ProductDAO();
        statisticDAO = new StatisticDAO();
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
        List<Product> products = safeProducts();

        request.setAttribute("totalCustomers", statisticDAO.getTotalCustomers());
        request.setAttribute("totalOrders", orders.size());
        request.setAttribute("pendingOrders", countOrdersByStatus(orders, OrderStatus.PENDING));
        request.setAttribute("processingOrders", countOrdersByStatus(orders, OrderStatus.PROCESSING));
        request.setAttribute("shippingOrders", countOrdersByStatus(orders, OrderStatus.SHIPPING));
        request.setAttribute("deliveredOrders", countOrdersByStatus(orders, OrderStatus.DELIVERED));
        request.setAttribute("cancelledOrders", countOrdersByStatus(orders, OrderStatus.CANCELLED));
        request.setAttribute("totalPayments", payments.size());
        request.setAttribute("pendingPayments", countPaymentsByStatus(payments, PaymentStatus::isPending));
        request.setAttribute("paidPayments", countPaymentsByStatus(payments, PaymentStatus::isPaid));
        request.setAttribute("failedPayments", countPaymentsByStatus(payments, PaymentStatus::isFailed));
        request.setAttribute("totalProducts", products.size());
        request.setAttribute("recentOrders", orders);
        request.setAttribute("payments", payments);

        // Overview analytics (same data set used by Admin)
        request.setAttribute("revenue", statisticDAO.getRevenue());
        request.setAttribute("costOfGoodsSold", statisticDAO.getCostOfGoodsSold());
        request.setAttribute("profit", statisticDAO.getProfit());
        request.setAttribute("totalProductSold", statisticDAO.getTotalProductSold(null, null));
        request.setAttribute("topProducts", statisticDAO.getTopProducts(5, null, null));
        request.setAttribute("topSpenders", statisticDAO.getTopSpenders(5, null, null));
        request.setAttribute("orderStatistics", statisticDAO.getOrderStatistics());

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

    private int countPaymentsByStatus(List<Payment> payments, java.util.function.Predicate<String> matcher) {
        int count = 0;
        for (Payment payment : payments) {
            if (payment != null && matcher.test(payment.getPaymentStatus())) {
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
