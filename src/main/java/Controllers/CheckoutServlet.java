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
import Services.OrderService;
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

        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            request.setAttribute("errorMessage", "Please enter shipping address and phone number.");
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        Order preview = orderService.reviewOrder(customerId, shippingAddress, phone, cart);
        if (preview == null) {
            request.setAttribute("errorMessage", "Cannot create order. Please check your cart again.");
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        // Create order directly - Bill will be created when staff confirms the order
        String orderId = orderService.checkout(customerId, shippingAddress, phone, cart);

        if (orderId == null) {
            request.setAttribute("errorMessage", "Checkout failed. Please check your information.");
            request.setAttribute("checkoutTotal", preview.getTotalAmount());
            forwardLayout(request, response, CHECKOUT_PAGE);
            return;
        }

        session.setAttribute("successMessage", "Order created successfully! Order ID: " + orderId);

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

        String[] selectedIds = (String[]) selectedIdsObject;
        Cart cart = new CartDAO().getActiveCart(customerId);
        if (cart == null) {
            return;
        }

        new CartItemDAO().deleteItemsByIds(cart.getCartId(), selectedIds);
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

    private void prepareCheckoutPage(HttpServletRequest request, HttpSession session, String customerId) {
        request.setAttribute("categories", new CategoryDAO().getAllCategories());

        Account user = (Account) session.getAttribute("USER");
        if (user != null) {
            request.setAttribute("shippingAddress", user.getAddress());
            request.setAttribute("phone", user.getPhone());
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
