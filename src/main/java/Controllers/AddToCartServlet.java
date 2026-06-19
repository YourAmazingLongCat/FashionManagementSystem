package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
import DALs.ProductDAO;
import Models.Account;
import Models.Cart;
import Models.CartItemView;
import Models.ProductVariant;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/cart/add")
public class AddToCartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Account acc = (Account) request.getSession().getAttribute("USER");

        if (acc == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String variantId = trim(request.getParameter("variantId"));
        String quantityStr = trim(request.getParameter("quantity"));
        String productId = trim(request.getParameter("productId"));

        if (variantId == null || variantId.isBlank()) {
            response.sendRedirect(request.getHeader("referer"));
            return;
        }

        int quantity = 1;
        try {
            quantity = Math.max(1, Integer.parseInt(quantityStr));
        } catch (NumberFormatException ignored) {}

        // Check stock availability
        ProductDAO productDAO = new ProductDAO();
        ProductVariant variant = productDAO.getVariantById(variantId);
        if (variant == null || variant.getStockQty() <= 0) {
            response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=variant-unavailable");
            return;
        }

        // Limit quantity to available stock
        quantity = Math.min(quantity, variant.getStockQty());

        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(acc.getAccountId());

        String cartId;
        if (cart == null) {
            cartId = cartDAO.createCart(acc.getAccountId());
        } else {
            cartId = cart.getCartId();
        }

        CartItemDAO itemDAO = new CartItemDAO();

        if (itemDAO.existsItem(cartId, variantId)) {
            itemDAO.increaseQuantity(cartId, variantId, quantity);
        } else {
            itemDAO.addItem(cartId, variantId, quantity);
        }

        // Update cart count in session for header display
        List<CartItemView> items = itemDAO.getCartItems(cartId);
        int cartCount = items.stream().mapToInt(CartItemView::getQuantity).sum();
        request.getSession().setAttribute("cartCount", cartCount);

        response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=added-to-cart");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
