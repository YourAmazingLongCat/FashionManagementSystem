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

        Account loggedUser = (Account) request.getSession().getAttribute("USER");
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // Lấy tham số
        String requestedVariantId = trim(request.getParameter("variantId"));
        String productId = trim(request.getParameter("productId"));
        String colorId = trim(request.getParameter("colorId"));
        String sizeId = trim(request.getParameter("sizeId"));
        String quantityParam = trim(request.getParameter("quantity"));

        // Nếu variantId không có, thử tìm theo productId + colorId + sizeId
        if ((requestedVariantId == null || requestedVariantId.isBlank()) 
                && productId != null && !productId.isBlank()) {
            requestedVariantId = findVariantIdByAttributes(productId, colorId, sizeId);
        }

        // Nếu vẫn không có, fallback lấy variant đầu tiên của sản phẩm
        if ((requestedVariantId == null || requestedVariantId.isBlank())
                && productId != null && !productId.isBlank()) {
            ProductDAO pDAO = new ProductDAO();
            Product p = pDAO.getProductById(productId);
            if (p != null && p.getVariants() != null && !p.getVariants().isEmpty()) {
                requestedVariantId = p.getVariants().get(0).getVariantId();
            }
        }

        // Vẫn không có variantId → redirect lỗi
        if (requestedVariantId == null || requestedVariantId.isBlank()) {
            String redirectUrl = request.getContextPath() 
                    + "/home/view-detail-product?productId=" + (productId != null ? productId : "")
                    + "&message=variant-required";
            response.sendRedirect(redirectUrl);
            return;
        }

        // Xác định số lượng
        int qty = 1;
        try {
            qty = Math.max(1, Integer.parseInt(quantityParam));
        } catch (NumberFormatException ignored) {
            // keep default = 1
        }

        ProductDAO productDAO = new ProductDAO();
        ProductVariant variant = productDAO.getVariantById(requestedVariantId);

        // Fallback: nếu variant không tồn tại, lấy variant đầu tiên của product
        if (variant == null) {
            Product product = productDAO.getProductById(productId);
            if (product != null && product.getVariants() != null && !product.getVariants().isEmpty()) {
                variant = product.getVariants().get(0);
                requestedVariantId = variant.getVariantId();
            } else {
                response.sendRedirect(request.getContextPath() 
                        + "/home/view-detail-product?productId=" + productId 
                        + "&message=variant-unavailable");
                return;
            }
        }

        int availableStock = variant.getAvailableQty();
        if (availableStock <= 0) {
            response.sendRedirect(request.getContextPath() 
                    + "/home/view-detail-product?productId=" + productId 
                    + "&message=variant-unavailable");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(loggedUser.getAccountId());

        String cartId = (cart == null) 
                ? cartDAO.createCart(loggedUser.getAccountId()) 
                : cart.getCartId();

        int currentQuantityInCart = cartDAO.getQuantityInCart(cartId, requestedVariantId);
        int totalRequested = currentQuantityInCart + qty;

        if (totalRequested > availableStock) {
            int maxCanAdd = Math.max(0, availableStock - currentQuantityInCart);
            if (maxCanAdd <= 0) {
                response.sendRedirect(request.getContextPath() 
                        + "/home/view-detail-product?productId=" + productId 
                        + "&message=cart-quantity-exceeded");
                return;
            }
            qty = maxCanAdd;
        }

        // Thêm hoặc cập nhật
        if (cartDAO.existsItem(cartId, requestedVariantId)) {
            cartDAO.increaseQuantity(cartId, requestedVariantId, qty);
        } else {
            cartDAO.addItem(cartId, requestedVariantId, qty);
        }

        // Cập nhật số lượng giỏ hàng trên session
        List<CartItemView> cartItems = cartDAO.getCartItems(cartId);
        int totalItems = cartItems.stream().mapToInt(CartItemView::getQuantity).sum();
        request.getSession().setAttribute("cartCount", totalItems);

        response.sendRedirect(request.getContextPath() 
                + "/home/view-detail-product?productId=" + productId 
                + "&message=added-to-cart");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Tìm variantId dựa trên productId, colorId, sizeId.
     */
    private String findVariantIdByAttributes(String productId, String colorId, String sizeId) {
        ProductDAO productDAO = new ProductDAO();
        Product product = productDAO.getProductById(productId);
        if (product == null || product.getVariants() == null) {
            return null;
        }
        for (ProductVariant v : product.getVariants()) {
            boolean colorMatch = (colorId == null || colorId.isBlank()) 
                                 || colorId.equals(v.getColorId());
            boolean sizeMatch = (sizeId == null || sizeId.isBlank()) 
                                || sizeId.equals(v.getSizeId());
            if (colorMatch && sizeMatch) {
                return v.getVariantId();
            }
        }
        return null;
    }
}