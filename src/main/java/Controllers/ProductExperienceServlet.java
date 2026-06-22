package Controllers;

import DALs.CategoryDAO;
import DALs.WishlistDAO;
import DALs.ProductDAO;
import DALs.CartDAO;
import DALs.CartItemDAO;
import Models.CartItem;
import Models.CartItemView;
import Models.Cart;
import Models.Category;
import Models.Product;
import Models.ProductVariant;
import Models.Account;
import Utils.SessionCartUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "ProductExperienceServlet", urlPatterns = {
    "/home/search",
    "/home/customer/toggle-wishlist",
    "/home/cart/add"
})
public class ProductExperienceServlet extends HttpServlet {

    private static final int SEARCH_PAGE_SIZE = 12;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/home/search".equals(request.getServletPath())) {
            showSearch(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/home/customer/toggle-wishlist".equals(servletPath)) {
            toggleWishlist(request, response);
            return;
        }
        if ("/home/cart/add".equals(servletPath)) {
            addToCart(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/home");
    }

    private void showSearch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        List<Product> allProducts = productDAO.getAllProducts().stream()
                .filter(product -> "Available".equalsIgnoreCase(product.getStatus()))
                .collect(Collectors.toList());

        String keyword = trim(request.getParameter("search"));
        String categoryId = trim(request.getParameter("categoryId"));
        String[] categoryParams = request.getParameterValues("category");
        String sort = trim(request.getParameter("sort"));
        String minPriceRaw = trim(request.getParameter("minPrice"));
        String maxPriceRaw = trim(request.getParameter("maxPrice"));
        int currentPage = parsePositiveInt(request.getParameter("page"), 1);
        BigDecimal minPrice = parseBigDecimal(minPriceRaw);
        BigDecimal maxPrice = parseBigDecimal(maxPriceRaw);

        List<String> selectedCategories = new ArrayList<>();
        if (categoryId != null && !categoryId.isBlank()) {
            selectedCategories.add(categoryId);
        }
        if (categoryParams != null) {
            for (String item : categoryParams) {
                if (item != null && !item.isBlank() && !selectedCategories.contains(item)) {
                    selectedCategories.add(item);
                }
            }
        }

        List<Product> filtered = allProducts.stream()
                .filter(product -> matchesKeyword(product, keyword))
                .filter(product -> selectedCategories.isEmpty() || selectedCategories.contains(product.getCategoryId()))
                .filter(product -> matchesPrice(productDAO.getDisplayPrice(product), minPrice, maxPrice))
                .sorted(buildComparator(sort, productDAO))
                .collect(Collectors.toList());

        int totalProduct = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalProduct / SEARCH_PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        int fromIndex = Math.max(0, (currentPage - 1) * SEARCH_PAGE_SIZE);
        int toIndex = Math.min(filtered.size(), fromIndex + SEARCH_PAGE_SIZE);
        List<Product> products = filtered.subList(fromIndex, toIndex);

        List<Category> categories = categoryDAO.getAllCategories();
        Set<String> selectedCategoryIds = new LinkedHashSet<>(selectedCategories);
        Set<String> wishlistProductIds = getWishlistProductIds(request);

        request.setAttribute("categories", categories);
        request.setAttribute("selectedCategoryIds", selectedCategoryIds);
        request.setAttribute("brands", new ArrayList<>());
        request.setAttribute("products", products);
        request.setAttribute("selectedCategories", selectedCategories);
        request.setAttribute("selectedBrands", new ArrayList<>());
        request.setAttribute("selectedTypes", new ArrayList<>());
        request.setAttribute("selectedSort", sort == null || sort.isBlank() ? "latest" : sort);
        request.setAttribute("isLatestSort", sort == null || sort.isBlank() || "latest".equalsIgnoreCase(sort));
        request.setAttribute("isPriceAscSort", "priceAsc".equalsIgnoreCase(sort));
        request.setAttribute("isPriceDescSort", "priceDesc".equalsIgnoreCase(sort));
        request.setAttribute("selectedMinPrice", minPriceRaw);
        request.setAttribute("selectedMaxPrice", maxPriceRaw);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("showing", products.size());
        request.setAttribute("totalProduct", totalProduct);
        request.setAttribute("query", buildQuery(request));
        request.setAttribute("productDAO", productDAO);
        request.setAttribute("wishlistProductIds", wishlistProductIds);
        request.setAttribute("cartCount", getCartCount(request));
        request.setAttribute("contentPage", "/Pages/Guest/Home/SearchAndFilter/SearchAndFilter.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    private void toggleWishlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"requiresLogin\":true}");
            return;
        }

        String productId = trim(request.getParameter("productId"));
        WishlistDAO wishlistDAO = new WishlistDAO();
        boolean inWishlist;
        if (wishlistDAO.isInWishlist(user.getAccountId(), productId)) {
            wishlistDAO.removeFromWishlist(user.getAccountId(), productId);
            inWishlist = false;
        } else {
            wishlistDAO.addToWishlist(user.getAccountId(), productId);
            inWishlist = true;
        }

        Set<String> wishlist = wishlistDAO.getWishlistProductIdsByAccountId(user.getAccountId());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"inWishlist\":" + inWishlist + ",\"count\":" + wishlist.size() + "}");
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account user = getLoggedInUser(request);
        
        // If user is logged in, redirect to proper cart servlet (database cart)
        if (user != null) {
            String variantId = trim(request.getParameter("variantId"));
            String quantity = trim(request.getParameter("quantity"));
            String productId = trim(request.getParameter("productId"));
            response.sendRedirect(request.getContextPath() + "/cart/add?variantId=" + variantId + "&quantity=" + quantity + "&productId=" + productId);
            return;
        }

        String productId = trim(request.getParameter("productId"));
        String variantId = trim(request.getParameter("variantId"));
        int quantity = Math.max(1, parsePositiveInt(request.getParameter("quantity"), 1));

        Product product = new ProductDAO().getProductById(productId);
        if (product == null || variantId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        ProductVariant selectedVariant = null;
        for (ProductVariant variant : product.getVariants()) {
            if (variantId.equals(variant.getVariantId())) {
                selectedVariant = variant;
                break;
            }
        }

        if (selectedVariant == null || selectedVariant.getStockQty() <= 0) {
            response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=variant-unavailable");
            return;
        }

        List<CartItem> cart = SessionCartUtil.getCart(request);
        CartItem existing = null;
        for (CartItem item : cart) {
            if (variantId.equals(item.getVariantId())) {
                existing = item;
                break;
            }
        }

        if (existing == null) {
            existing = new CartItem();
            existing.setProductId(product.getProductId());
            existing.setProductName(product.getName());
            existing.setProductImageUrl(product.getPrimaryImageUrl());
            existing.setVariantId(selectedVariant.getVariantId());
            existing.setSizeId(selectedVariant.getSizeId());
            existing.setSizeName(selectedVariant.getSizeName());
            existing.setColorId(selectedVariant.getColorId());
            existing.setColorName(selectedVariant.getColorName());
            existing.setUnitPrice(selectedVariant.getPriceOverride() != null ? selectedVariant.getPriceOverride() : product.getBasePrice());
            existing.setQuantity(0);
            cart.add(existing);
        }

        existing.setQuantity(Math.min(selectedVariant.getStockQty(), existing.getQuantity() + quantity));
        request.getSession().setAttribute(SessionCartUtil.CART_SESSION_KEY, cart);
        response.sendRedirect(request.getContextPath() + "/home/view-detail-product?productId=" + productId + "&message=added-to-cart");
    }

    private boolean matchesKeyword(Product product, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return (product.getName() != null && product.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                || (product.getDescription() != null && product.getDescription().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                || (product.getCategoryName() != null && product.getCategoryName().toLowerCase(Locale.ROOT).contains(normalizedKeyword));
    }

    private boolean matchesPrice(BigDecimal value, BigDecimal minPrice, BigDecimal maxPrice) {
        if (value == null) {
            return false;
        }
        if (minPrice != null && value.compareTo(minPrice) < 0) {
            return false;
        }
        if (maxPrice != null && value.compareTo(maxPrice) > 0) {
            return false;
        }
        return true;
    }

    private Comparator<Product> buildComparator(String sort, ProductDAO productDAO) {
        if ("priceAsc".equalsIgnoreCase(sort)) {
            return Comparator.comparing(productDAO::getDisplayPrice, Comparator.nullsLast(BigDecimal::compareTo));
        }
        if ("priceDesc".equalsIgnoreCase(sort)) {
            return Comparator.comparing(productDAO::getDisplayPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed();
        }
        return Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }

    private String buildQuery(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .filter(entry -> !"page".equals(entry.getKey()))
                .flatMap(entry -> java.util.Arrays.stream(entry.getValue()).map(value -> entry.getKey() + "=" + value))
                .collect(Collectors.joining("&"));
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
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

    private Set<String> getWishlistProductIds(HttpServletRequest request) {
        Account user = getLoggedInUser(request);
        if (user == null) {
            return new java.util.LinkedHashSet<>();
        }
        return new WishlistDAO().getWishlistProductIdsByAccountId(user.getAccountId());
    }

    private Account getLoggedInUser(HttpServletRequest request) {
        Object userObject = request.getSession(false) != null ? request.getSession(false).getAttribute("USER") : null;
        return userObject instanceof Account ? (Account) userObject : null;
    }
}
