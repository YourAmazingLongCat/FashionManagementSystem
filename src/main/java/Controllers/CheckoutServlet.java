package Controllers;

import java.io.IOException;
import java.util.List;

import DALs.CartDAO;
import DALs.CartItemDAO;
import DALs.CategoryDAO;
import Models.Account;
import Models.Cart;
import Models.CartItem;
import Models.Order;
import Models.Wallet;
import Services.OrderService;
import Services.PaymentService;
import Utils.PaymentMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Compatibility route for old links that still point to /customer/checkout.
 *
 * The project now uses one customer Order page for both cases:
 * - immediately after the Cart Checkout button creates a Pending order;
 * - when the customer returns later from Order History.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = {"/customer/checkout"})
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        prepareCheckoutPage(request, session, customerId);

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null && !cart.isEmpty()) {
            Order preview = orderService.reviewOrder(customerId, "Preview", "Preview", cart);
            if (preview != null) {
                request.setAttribute("checkoutTotal", preview.getTotalAmount());
            }
        }

        forwardLayout(request, response, CHECKOUT_PAGE);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        redirectToOrderPage(request, response);
    }

    private void redirectToOrderPage(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (getCustomerId(session) == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            request.setAttribute("errorMessage", "Your cart is empty.");
            prepareCheckoutPage(request, session, customerId);
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        prepareCheckoutPage(request, session, customerId);
        String shippingAddress = trim(request.getParameter("shippingAddress"));
        String phone = trim(request.getParameter("phone"));
        String paymentMethod = normalizePaymentMethod(request.getParameter("paymentMethod"));

        Account user = (Account) session.getAttribute("USER");
        if (user != null) {
            if (isEmpty(shippingAddress)) {
                shippingAddress = user.getAddress();
            }
            if (isEmpty(phone)) {
                phone = user.getPhone();
            }
        }

        request.setAttribute("shippingAddress", shippingAddress);
        request.setAttribute("phone", phone);
        request.setAttribute("paymentMethod", paymentMethod);

        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            request.setAttribute("errorMessage", "Please enter shipping address and phone number.");
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        Order preview = orderService.reviewOrder(customerId, shippingAddress, phone, cart);
        if (preview == null) {
            request.setAttribute("errorMessage", "Cannot create order. Please check your cart again.");
            request.setAttribute("wallet", paymentService.getOrCreateWallet(customerId));
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        if (PaymentMethod.WALLET.equals(paymentMethod)
                && !paymentService.canPayAmountByWallet(customerId, preview.getTotalAmount())) {
            request.setAttribute("errorMessage", "Your wallet balance is not enough. Please deposit more money or choose COD.");
            request.setAttribute("checkoutTotal", preview.getTotalAmount());
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        String orderId = orderService.checkout(customerId, shippingAddress, phone, cart);

        if (orderId == null) {
            request.setAttribute("errorMessage", "Checkout failed. Please check your information.");
            request.setAttribute("checkoutTotal", preview.getTotalAmount());
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        boolean paymentHandled;
        if (PaymentMethod.WALLET.equals(paymentMethod)) {
            paymentHandled = paymentService.payOrderByWallet(customerId, orderId);
            session.setAttribute(paymentHandled ? "successMessage" : "errorMessage",
                    paymentHandled
                            ? "Order created and paid successfully by Wallet."
                            : "Order was created, but Wallet payment failed. Please pay again from order detail.");
        } else if (PaymentMethod.VNPAY.equals(paymentMethod)) {
            paymentHandled = paymentService.createVNPayPaymentForOrder(customerId, orderId);
            session.setAttribute(paymentHandled ? "successMessage" : "errorMessage",
                    paymentHandled
                            ? "Order created. VNPay payment record has been created and is waiting for payment confirmation."
                            : "Order created, but VNPay payment record could not be created.");
        } else {
            paymentHandled = paymentService.createCODPaymentForOrder(customerId, orderId);
            session.setAttribute(paymentHandled ? "successMessage" : "errorMessage",
                    paymentHandled
                            ? "Order created. COD payment record has been created and will become Paid when delivered."
                            : "Order created, but COD payment record could not be created.");
        }

        removeCheckedOutItemsFromDatabaseCart(session, customerId);
        session.removeAttribute("cart");
        session.removeAttribute("checkoutCartItemIds");
        session.setAttribute("cartCount", 0);

        response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + orderId);
    }

    private void removeCheckedOutItemsFromDatabaseCart(HttpSession session, String customerId) {
        Object selectedIdsObject = session.getAttribute("checkoutCartItemIds");
        if (!(selectedIdsObject instanceof String[])) {
            return;
        }

        if (isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId);
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

    private void prepareCheckoutPage(HttpServletRequest request, HttpSession session, String customerId) {
        Wallet wallet = paymentService.getOrCreateWallet(customerId);
        request.setAttribute("wallet", wallet);
        request.setAttribute("categories", new CategoryDAO().getAllCategories());

        Account user = (Account) session.getAttribute("USER");
        if (user != null) {
            request.setAttribute("shippingAddress", user.getAddress());
            request.setAttribute("phone", user.getPhone());
            request.setAttribute("paymentMethod", PaymentMethod.COD);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
