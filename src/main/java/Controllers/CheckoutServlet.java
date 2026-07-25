package Controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DALs.CartDAO;
import DALs.CartItemDAO;
import DALs.CategoryDAO;
import Models.Account;
import Models.Cart;
import Models.CartItem;
import Models.CartItemView;
import Services.OrderService;
import Services.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/customer/checkout"})
public class CheckoutServlet extends HttpServlet {

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String CHECKOUT_PAGE = "/Pages/Customer/checkout.jsp";

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // Check if we have checkout cart items in session
        List<CartItem> checkoutCart = (List<CartItem>) session.getAttribute("checkoutCart");
        if (checkoutCart == null || checkoutCart.isEmpty()) {
            session.setAttribute("errorMessage", "No items selected for checkout.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        BigDecimal totalAmount = (BigDecimal) session.getAttribute("checkoutTotal");
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
            for (CartItem item : checkoutCart) {
                totalAmount = totalAmount.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        Account user = (Account) session.getAttribute("USER");
        String address = user != null ? user.getAddress() : "";
        String phone = user != null ? user.getPhone() : "";

        request.setAttribute("shippingAddress", address);
        request.setAttribute("phone", phone);
        request.setAttribute("checkoutTotal", totalAmount);
        request.setAttribute("categories", new CategoryDAO().getAllCategories());

        forwardLayout(request, response, CHECKOUT_PAGE);
    }

    @Override
    @SuppressWarnings("unchecked")
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

        // Check if this is a place order action
        String action = request.getParameter("action");
        if ("placeOrder".equals(action)) {
            doPlaceOrder(request, response, session, customerId);
            return;
        }

        // Get selected items from form
        String selectedItemsStr = request.getParameter("selectedItemsList");
        if (selectedItemsStr == null || selectedItemsStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Please select at least one product to checkout.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<String> selectedItemIds = Arrays.asList(selectedItemsStr.split(","));
        if (selectedItemIds.isEmpty()) {
            session.setAttribute("errorMessage", "Please select at least one product to checkout.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Load cart items from database
        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(customerId);
        if (cart == null) {
            session.setAttribute("errorMessage", "Your cart is empty.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        CartItemDAO cartItemDAO = new CartItemDAO();
        String[] itemIds = selectedItemIds.toArray(new String[0]);
        List<CartItemView> selectedCartItems = cartItemDAO.getCartItemsByIds(cart.getCartId(), itemIds);

        if (selectedCartItems == null || selectedCartItems.isEmpty()) {
            session.setAttribute("errorMessage", "Selected items are no longer available.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Convert to CartItem list
        List<CartItem> checkoutCart = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
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
            totalAmount = totalAmount.add(BigDecimal.valueOf(viewItem.getSubtotal()));
        }

        // Store in session
        session.setAttribute("checkoutCart", checkoutCart);
        session.setAttribute("checkoutCartIds", itemIds);
        session.setAttribute("checkoutTotal", totalAmount);

        // Get user info for pre-fill
        Account user = (Account) session.getAttribute("USER");
        String address = user != null && user.getAddress() != null ? user.getAddress() : "";
        String phone = user != null && user.getPhone() != null ? user.getPhone() : "";

        request.setAttribute("shippingAddress", address);
        request.setAttribute("phone", phone);
        request.setAttribute("checkoutTotal", totalAmount);
        request.setAttribute("categories", new CategoryDAO().getAllCategories());

        forwardLayout(request, response, CHECKOUT_PAGE);
    }

    private void forwardLayout(HttpServletRequest request, HttpServletResponse response, String contentPage)
            throws ServletException, IOException {
        request.setAttribute("contentPage", contentPage);
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

    @SuppressWarnings("unchecked")
    protected void doPlaceOrder(HttpServletRequest request, HttpServletResponse response, HttpSession session, String customerId)
            throws ServletException, IOException {

        String[] selectedItemIds = (String[]) session.getAttribute("checkoutCartIds");
        if (selectedItemIds == null || selectedItemIds.length == 0) {
            session.setAttribute("errorMessage", "Your checkout session has expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        String shippingAddress = trim(request.getParameter("shippingAddress"));
        String phone = trim(request.getParameter("phone"));

        Account user = (Account) session.getAttribute("USER");
        if (user != null) {
            if (isEmpty(shippingAddress)) {
                shippingAddress = user.getAddress();
            }
            if (isEmpty(phone)) {
                phone = user.getPhone();
            }
        }

        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            request.setAttribute("errorMessage", "Please enter shipping address and phone number.");
            request.setAttribute("categories", new CategoryDAO().getAllCategories());
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        // Get cart and cart items from database
        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(customerId);
        if (cart == null) {
            session.setAttribute("errorMessage", "Your cart is empty.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Get payment method
        String paymentMethod = trim(request.getParameter("paymentMethod"));
        if (isEmpty(paymentMethod)) {
            paymentMethod = "COD";
        }

        CartItemDAO cartItemDAO = new CartItemDAO();
        List<CartItemView> selectedCartItems = cartItemDAO.getCartItemsByIds(cart.getCartId(), selectedItemIds);

        if (selectedCartItems == null || selectedCartItems.isEmpty()) {
            session.setAttribute("errorMessage", "Selected items are no longer available.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Convert to CartItem list
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

        // Create order with stock reservation (using createPendingOrderFromCart)
        String orderId = orderService.createPendingOrderFromCart(
                customerId, shippingAddress, phone, checkoutCart,
                cart.getCartId(), selectedItemIds);

        if (orderId == null) {
            request.setAttribute("errorMessage", "Checkout failed. Please check your cart again.");
            request.setAttribute("categories", new CategoryDAO().getAllCategories());
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        // Create payment record based on selected payment method
        boolean paymentCreated = createPaymentRecord(customerId, orderId, paymentMethod, checkoutCart);

        // Clean up session
        session.removeAttribute("checkoutCart");
        session.removeAttribute("checkoutCartIds");
        session.removeAttribute("checkoutItems");

        // Update cart count
        List<CartItemView> remainingItems = cartItemDAO.getCartItems(cart.getCartId());
        int cartCount = remainingItems.stream().mapToInt(CartItemView::getQuantity).sum();
        session.setAttribute("cartCount", cartCount);

        // For VNPay, redirect to VNPay payment page
        if ("VNPay".equalsIgnoreCase(paymentMethod) && paymentCreated) {
            session.setAttribute("successMessage", "Order created! Redirecting to VNPay payment...");
            response.sendRedirect(request.getContextPath() + "/customer/vnpay/start?orderId=" + orderId);
            return;
        }

        // For Wallet, check and process
        if ("Wallet".equalsIgnoreCase(paymentMethod)) {
            PaymentService paymentService = new PaymentService();
            if (paymentService.canPayOrderByWallet(customerId, orderId)) {
                paymentService.payOrderByWallet(customerId, orderId);
                session.setAttribute("successMessage", "Order created and paid with wallet!");
            } else {
                session.setAttribute("errorMessage", "Insufficient wallet balance. Order created with pending payment.");
            }
        }

        session.setAttribute("successMessage", "Order created successfully! Order ID: " + orderId);
        response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + orderId);
    }

    private boolean createPaymentRecord(String accountId, String orderId, String paymentMethod, List<CartItem> checkoutCart) {
        PaymentService paymentService = new PaymentService();

        if ("VNPay".equalsIgnoreCase(paymentMethod)) {
            return paymentService.createVNPayPaymentForOrder(accountId, orderId);
        } else if ("Wallet".equalsIgnoreCase(paymentMethod)) {
            return paymentService.createCODPaymentForOrder(accountId, orderId);
        } else {
            // COD
            return paymentService.createCODPaymentForOrder(accountId, orderId);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
