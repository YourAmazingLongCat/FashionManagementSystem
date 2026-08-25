package Controllers;

import DALs.CartDAO;
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

        String[] selectedIds = request.getParameterValues("selectedItems");
        if (selectedIds == null || selectedIds.length == 0) {
            session.setAttribute("errorMessage", "Please select at least one product before checkout.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        Cart activeCart = cartDAO.getActiveCart(account.getAccountId());

        if (activeCart == null) {
            session.setAttribute("errorMessage", "Your cart is empty.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<CartItemView> selectedViews = cartDAO.getCartItemsByIds(activeCart.getCartId(), selectedIds);
        if (selectedViews == null || selectedViews.isEmpty()) {
            session.setAttribute("errorMessage", "Selected cart items are invalid or no longer available.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Chuyển đổi sang CartItem để gửi sang OrderService
        List<CartItem> checkoutItems = new ArrayList<>();
        for (CartItemView view : selectedViews) {
            CartItem item = new CartItem();
            item.setVariantId(view.getVariantId());
            item.setProductName(view.getProductName());
            item.setProductImageUrl(view.getImageUrl());
            item.setSizeName(view.getSizeName());
            item.setColorName(view.getColorName());
            item.setQuantity(view.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(view.getPrice()));
            checkoutItems.add(item);
        }

        // Tạo đơn hàng (Pending) và xóa các item đã chọn khỏi giỏ
        String orderId = orderService.createPendingOrderFromCart(
                account.getAccountId(),
                "",
                account.getPhone(),
                checkoutItems,
                activeCart.getCartId(),
                selectedIds
        );

        if (orderId == null) {
            session.setAttribute("errorMessage",
                    "Checkout failed because the cart changed or one or more products no longer have enough available stock.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Cập nhật lại số lượng giỏ hàng hiển thị
        List<CartItemView> remainingItems = cartDAO.getCartItems(activeCart.getCartId());
        int remainingCount = remainingItems.stream()
                .mapToInt(CartItemView::getQuantity)
                .sum();
        session.setAttribute("cartCount", remainingCount);

        // Xóa các session tạm
        session.setAttribute("pendingCheckoutOrderId", orderId);
        session.removeAttribute("cart");
        session.removeAttribute("checkoutCartItemIds");
        session.removeAttribute("successMessage");
        session.removeAttribute("errorMessage");

        // Chuyển sang trang chi tiết đơn hàng
        response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + orderId);
    }
}