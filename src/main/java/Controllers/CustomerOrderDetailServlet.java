package Controllers;

import Models.Account;
import Models.Order;
import Models.OrderItem;
import Models.Payment;
import Models.Wallet;
import Services.OrderService;
import Services.PaymentService;
import Utils.OrderStatus;
import Utils.PaymentMethod;
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

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String ORDER_PAGE = "/Pages/Customer/orderDetail.jsp";

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
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        if (isEmpty(orderId)) {
            Object pendingOrderId = session.getAttribute("pendingCheckoutOrderId");
            orderId = pendingOrderId == null ? null : trim(pendingOrderId.toString());
        }

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        prepareOrderPage(request, customerId, orderId);
        forwardLayout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = trim(request.getParameter("orderId"));
        if (isEmpty(orderId)) {
            session.setAttribute("errorMessage", "Missing order ID.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        Order order = orderService.viewOrderDetailForCustomer(customerId, orderId);
        if (order == null) {
            session.setAttribute("errorMessage", "Order not found.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        String action = trim(request.getParameter("action"));
        if ("updateShipping".equals(action)) {
            updateShippingInformation(request, response, session, customerId, orderId);
            return;
        }

        placeOrder(request, response, session, customerId, order);
    }

    private void placeOrder(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String customerId, Order order) throws IOException {
        String orderId = order.getOrderId();
        String detailUrl = request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId;

        if (!OrderStatus.PENDING.equals(order.getOrderStatus())) {
            session.setAttribute("errorMessage",
                    "Only a Pending order can be placed by the customer.");
            response.sendRedirect(detailUrl);
            return;
        }

        Payment existingPayment = paymentService.getPaymentByOrderId(orderId);
        if (existingPayment != null) {
            session.setAttribute("errorMessage", "This order has already been placed.");
            response.sendRedirect(detailUrl);
            return;
        }

        String shippingAddress = trim(request.getParameter("shippingAddress"));
        String phone = trim(request.getParameter("phone"));
        String paymentMethod = normalizePaymentMethod(
                request.getParameter("paymentMethod"));

        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            session.setAttribute("errorMessage",
                    "Please enter the shipping address and phone number.");
            response.sendRedirect(detailUrl);
            return;
        }

        boolean deliveryUpdated = orderService.updateDeliveryInformationForCustomer(
                customerId, orderId, shippingAddress, phone);
        if (!deliveryUpdated) {
            session.setAttribute("errorMessage",
                    "Shipping information could not be saved because the order status changed.");
            response.sendRedirect(detailUrl);
            return;
        }

        boolean placed;
        if (PaymentMethod.WALLET.equals(paymentMethod)) {
            placed = paymentService.payOrderByWallet(customerId, orderId);
            if (!placed) {
                session.setAttribute("errorMessage",
                        "Wallet payment was not completed. Deposit enough balance and place the order again.");
                response.sendRedirect(detailUrl);
                return;
            }
        } else if (PaymentMethod.VNPAY.equals(paymentMethod)) {
            placed = paymentService.createVNPayPaymentForOrder(customerId, orderId);
        } else {
            placed = paymentService.createCODPaymentForOrder(customerId, orderId);
        }

        if (!placed) {
            session.setAttribute("errorMessage",
                    "The order information was saved, but the payment method could not be created. Please try again.");
            response.sendRedirect(detailUrl);
            return;
        }

        session.removeAttribute("pendingCheckoutOrderId");
        session.setAttribute("successMessage", "Order placed successfully.");
        response.sendRedirect(detailUrl);
    }

    private void updateShippingInformation(HttpServletRequest request,
            HttpServletResponse response, HttpSession session,
            String customerId, String orderId) throws IOException {
        String detailUrl = request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId;
        String shippingAddress = trim(request.getParameter("shippingAddress"));
        String phone = trim(request.getParameter("phone"));

        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            session.setAttribute("errorMessage",
                    "Please enter the shipping address and phone number.");
            response.sendRedirect(detailUrl);
            return;
        }

        boolean updated = orderService.updateDeliveryInformationForCustomer(
                customerId, orderId, shippingAddress, phone);

        session.setAttribute(updated ? "successMessage" : "errorMessage",
                updated
                        ? "Shipping information updated successfully."
                        : "Shipping information cannot be changed after the order starts shipping.");
        response.sendRedirect(detailUrl);
    }

    private void prepareOrderPage(HttpServletRequest request,
            String customerId, String orderId) {
        Order order = orderService.viewOrderDetailForCustomer(customerId, orderId);

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            return;
        }

        List<OrderItem> orderItems
                = orderService.viewOrderItemsForCustomer(customerId, orderId);
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        Wallet wallet = paymentService.getOrCreateWallet(customerId);

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("payment", payment);
        request.setAttribute("wallet", wallet);
        request.setAttribute("orderPlaced", payment != null);
        request.setAttribute("canEditDelivery",
                orderService.canEditDeliveryInformation(order));
    }

    private void forwardLayout(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("contentPage", ORDER_PAGE);
        request.getRequestDispatcher(LAYOUT_PAGE).forward(request, response);
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString();
        }

        Object user = session.getAttribute("USER");
        if (user instanceof Account) {
            return ((Account) user).getAccountId();
        }

        return null;
    }

    private String normalizePaymentMethod(String value) {
        if (PaymentMethod.WALLET.equals(value)) {
            return PaymentMethod.WALLET;
        }
        if (PaymentMethod.VNPAY.equals(value)) {
            return PaymentMethod.VNPAY;
        }
        return PaymentMethod.COD;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
