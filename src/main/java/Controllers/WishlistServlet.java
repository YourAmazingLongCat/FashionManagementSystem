package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
import DALs.ProductDAO;
import DALs.WishlistDAO;
import Models.Account;
import Models.Cart;
import Models.CartItemView;
import Models.Product;
import Utils.SessionCartUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "WishlistServlet", urlPatterns = {"/wishlist"})
public class WishlistServlet extends HttpServlet {

    private static final int ITEMS_PER_PAGE = 12;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        int currentPage = parsePositiveInt(request.getParameter("page"), 1);

        WishlistDAO wishlistDAO = new WishlistDAO();
        Set<String> wishlistProductIds = wishlistDAO.getWishlistProductIdsByAccountId(user.getAccountId());

        ProductDAO productDAO = new ProductDAO();
        List<Product> allWishlistProducts = new ArrayList<>();
        for (String productId : wishlistProductIds) {
            Product product = productDAO.getProductById(productId);
            if (product != null) {
                allWishlistProducts.add(product);
            }
        }

        int totalProduct = allWishlistProducts.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalProduct / ITEMS_PER_PAGE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = Math.max(0, (currentPage - 1) * ITEMS_PER_PAGE);
        int toIndex = Math.min(allWishlistProducts.size(), fromIndex + ITEMS_PER_PAGE);
        List<Product> wishlistProducts = fromIndex < toIndex 
            ? allWishlistProducts.subList(fromIndex, toIndex) 
            : new ArrayList<>();

        request.setAttribute("wishlistProducts", wishlistProducts);
        request.setAttribute("productDAO", productDAO);
        request.setAttribute("wishlistProductIds", wishlistProductIds);
        request.setAttribute("cartCount", getCartCount(request));
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("showing", wishlistProducts.size());
        request.setAttribute("totalProduct", totalProduct);
        request.setAttribute("contentPage", "/Pages/Guest/Home/Content/Wishlist.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private Account getLoggedInUser(HttpServletRequest request) {
        Object userObject = request.getSession(false) != null ? request.getSession(false).getAttribute("USER") : null;
        return userObject instanceof Account ? (Account) userObject : null;
    }

    private int getCartCount(HttpServletRequest request) {
        Account user = getLoggedInUser(request);
        if (user != null) {
            CartDAO cartDAO = new CartDAO();
            Cart cart = cartDAO.getActiveCart(user.getAccountId());
            if (cart != null) {
                CartItemDAO itemDAO = new CartItemDAO();
                List<CartItemView> items = itemDAO.getCartItems(cart.getCartId());
                return items.stream().mapToInt(CartItemView::getQuantity).sum();
            }
            return 0;
        }
        return SessionCartUtil.getCartCount(request);
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
