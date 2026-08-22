package Controllers;

import DALs.CartDAO;
import DALs.ProductDAO;
import Models.Account;
import Models.Cart;
import Models.CartItemView;
import Models.Product;
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
        String productId = trim(request.getParameter("productId"));
        String colorId = trim(request.getParameter("colorId"));
        String sizeId = trim(request.getParameter("sizeId"));
        String quantityStr = trim(request.getParameter("quantity"));

        
        if ((variantId == null || variantId.isBlank()) && productId != null && !productId.isBlank()) {
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.getProductById(productId);
            if (product != null && product.getVariants() != null) {
                for (ProductVariant v : product.getVariants()) {
                    boolean colorMatch = (colorId == null || colorId.isBlank()) ||
                                         (colorId.equals(v.getColorId()));
                    boolean sizeMatch = (sizeId == null || sizeId.isBlank()) ||
                                        (sizeId.equals(v.getSizeId()));
                    if (colorMatch && sizeMatch) {
                        variantId = v.getVariantId();
                        break;
                    }
                }
            }
        }

        
        if (variantId == null || variantId.isBlank()) {
            String redirectUrl = request.getContextPath() + "/home/view-detail-product?productId=" + (productId != null ? productId : "") + "&message=variant-required";
            response.sendRedirect(redirectUrl);
            return;
        }

        int quantity = 1;
        try {
            quantity = Math.max(1, Integer.parseInt(quantityStr));
        } catch (NumberFormatException ignored) {}

        ProductDAO productDAO = new ProductDAO();
        ProductVariant variant = productDAO.getVariantById(variantId);
        if (variant == null) {
            
            Product product = productDAO.getProductById(productId);
            if (product != null && product.getVariants() != null && !product.getVariants().isEmpty()) {
                variant = product.getVariants().get(0);
                variantId = variant.getVariantId();
            } else {
                response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=variant-unavailable");
                return;
            }
        }

        int availableStock = variant.getAvailableQty();
        if (availableStock <= 0) {
            response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=variant-unavailable");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(acc.getAccountId());

        String cartId;
        if (cart == null) {
            cartId = cartDAO.createCart(acc.getAccountId());
        } else {
            cartId = cart.getCartId();
        }

        int currentCartQty = cartDAO.getQuantityInCart(cartId, variantId);
        int totalRequestedQty = currentCartQty + quantity;

        if (totalRequestedQty > availableStock) {
            int maxCanAdd = availableStock - currentCartQty;
            if (maxCanAdd <= 0) {
                response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=cart-quantity-exceeded");
                return;
            }
            quantity = maxCanAdd;
        }

        if (cartDAO.existsItem(cartId, variantId)) {
            cartDAO.increaseQuantity(cartId, variantId, quantity);
        } else {
            cartDAO.addItem(cartId, variantId, quantity);
        }

        List<CartItemView> items = cartDAO.getCartItems(cartId);
        int cartCount = items.stream().mapToInt(CartItemView::getQuantity).sum();
        request.getSession().setAttribute("cartCount", cartCount);

        response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=added-to-cart");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}