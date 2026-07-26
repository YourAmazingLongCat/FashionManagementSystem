package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
import Models.Account;
import Models.Cart;
import Models.CartItem;
import Models.CartItemView;
import Services.OrderService;
import Services.PaymentService;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CartCheckoutServlet", urlPatterns = {"/cart/checkout"})
public class CartCheckoutServlet extends HttpServlet {

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
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("USER");

        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String[] selectedItems = request.getParameterValues("selectedItems");
        if (selectedItems == null || selectedItems.length == 0) {
            session.setAttribute("errorMessage",
                    "Please select at least one product before checkout.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Get payment method from form
        String paymentMethod = trimOrNull(request.getParameter("paymentMethod"));
        if (isEmpty(paymentMethod)) {
            paymentMethod = PaymentMethod.COD;
        }

        // Validate payment method
        if (!isValidPaymentMethod(paymentMethod)) {
            session.setAttribute("errorMessage", "Invalid payment method selected.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Validate: require shipping address and phone
        String address = trimOrNull(request.getParameter("shippingAddress"));
        String phone = trimOrNull(request.getParameter("phone"));

        // If not provided in form, try to use from profile
        if (isEmpty(address)) {
            address = trimOrNull(account.getAddress());
        }
        if (isEmpty(phone)) {
            phone = trimOrNull(account.getPhone());
        }

        // Still missing? Redirect to checkout page to fill in
        if (isEmpty(address) || isEmpty(phone)) {
            session.setAttribute("errorMessage",
                    "Please provide your shipping address and phone number before placing an order.");
            response.sendRedirect(request.getContextPath() + "/customer/checkout");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(account.getAccountId());

        if (cart == null) {
            session.setAttribute("errorMessage", "Your cart is empty.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        CartItemDAO cartItemDAO = new CartItemDAO();
        List<CartItemView> selectedCartItems
                = cartItemDAO.getCartItemsByIds(cart.getCartId(), selectedItems);

        if (selectedCartItems == null || selectedCartItems.isEmpty()) {
            session.setAttribute("errorMessage",
                    "Selected cart items are invalid or no longer available.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<CartItem> checkoutCart = new ArrayList<>();
        for (CartItemView viewItem : selectedCartItems) {
            CartItem item = new CartItem();
            item.setVariantId(viewItem.getVariantId());
            item.setProductName(viewItem.getProductName());
            item.setProductImageUrl(viewItem.getImageUrl());
            item.setSizeName(viewItem.getSizeName());
            item.setColorName(viewItem.getColorName());
            item.setQuantity(viewItem.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(viewItem.getPrice()));
            checkoutCart.add(item);
        }

        /*
         * The business rule is applied here, at the actual Cart Checkout
         * button. This transaction creates the Pending order, reserves stock,
         * inserts OrderItems and removes the selected CartItems together.
         */
        String orderId = orderService.createPendingOrderFromCart(
                account.getAccountId(),
                address,
                phone,
                checkoutCart,
                cart.getCartId(),
                selectedItems
        );

        if (orderId == null) {
            session.setAttribute("errorMessage",
                    "Checkout failed because the cart changed or one or more products no longer have enough available stock.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Create payment record based on selected payment method
        String paymentResult = createPaymentRecord(account.getAccountId(), orderId, paymentMethod, checkoutCart);

        // For VNPay, redirect to VNPay payment page
        if (PaymentMethod.VNPAY.equalsIgnoreCase(paymentMethod)) {
            session.setAttribute("pendingCheckoutOrderId", orderId);
            session.removeAttribute("cart");
            session.removeAttribute("checkoutCartItemIds");
            session.removeAttribute("successMessage");
            session.removeAttribute("errorMessage");
            // Redirect to VNPay start servlet
            response.sendRedirect(request.getContextPath() + "/customer/vnpay/start?orderId=" + orderId);
            return;
        }

        // Refresh the header badge using only the products still in the cart.
        List<CartItemView> remainingItems = cartItemDAO.getCartItems(cart.getCartId());
        int remainingCount = remainingItems.stream()
                .mapToInt(CartItemView::getQuantity)
                .sum();

        session.setAttribute("cartCount", remainingCount);
        session.setAttribute("pendingCheckoutOrderId", orderId);
        session.removeAttribute("cart");
        session.removeAttribute("checkoutCartItemIds");
        session.removeAttribute("successMessage");
        session.removeAttribute("errorMessage");

        response.sendRedirect(request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId);
    }

    private String createPaymentRecord(String accountId, String orderId, String paymentMethod, List<CartItem> checkoutCart) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : checkoutCart) {
            totalAmount = totalAmount.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Create payment record based on method
        if (PaymentMethod.VNPAY.equalsIgnoreCase(paymentMethod)) {
            return paymentService.createVNPayPaymentForOrder(accountId, orderId) ? orderId : null;
        } else if (PaymentMethod.WALLET.equalsIgnoreCase(paymentMethod)) {
            // For wallet, check if user has sufficient balance
            if (!paymentService.canPayAmountByWallet(accountId, totalAmount)) {
                return "INSUFFICIENT_WALLET_BALANCE";
            }
            // DON'T create payment record here - payOrderByWallet will create WALLET payment
            // Process wallet payment immediately (this creates the WALLET payment)
            if (!paymentService.payOrderByWallet(accountId, orderId)) {
                return null;
            }
            return orderId;
        } else {
            // COD - create payment record
            paymentService.createCODPaymentForOrder(accountId, orderId);
            return orderId;
        }
    }

    private boolean isValidPaymentMethod(String paymentMethod) {
        if (isEmpty(paymentMethod)) {
            return false;
        }
        String method = paymentMethod.trim();
        return PaymentMethod.VNPAY.equalsIgnoreCase(method)
                || PaymentMethod.WALLET.equalsIgnoreCase(method)
                || PaymentMethod.COD.equalsIgnoreCase(method);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
