package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
import Models.Account;
import Models.Cart;
import Models.CartItem;
import Models.CartItemView;
import Services.OrderService;
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

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
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
                "",
                account.getPhone(),
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

        // Refresh the header badge using only the products still in the cart.
        List<CartItemView> remainingItems = cartItemDAO.getCartItems(cart.getCartId());
        int remainingCount = remainingItems.stream()
                .mapToInt(CartItemView::getQuantity)
                .sum();

        session.setAttribute("cartCount", remainingCount);
        session.setAttribute("pendingCheckoutOrderId", orderId);
        session.removeAttribute("cart");
        session.removeAttribute("checkoutCartItemIds");

        // A successful Cart Checkout must not display a toast/notification.
        // The customer is taken directly to the single Order page instead.
        session.removeAttribute("successMessage");
        session.removeAttribute("errorMessage");

        response.sendRedirect(request.getContextPath()
                + "/customer/order-detail?orderId=" + orderId);
    }
}
