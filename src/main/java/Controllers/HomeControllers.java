package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
import DALs.CategoryDAO;
import DALs.CommentDAO;
import DALs.WishlistDAO;
import DALs.ProductDAO;
import Models.Account;
import Models.Cart;
import Models.CartItemView;
import Models.Category;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller xử lý trang chủ và chi tiết sản phẩm cho khách.
 */
@WebServlet(name = "HomeControllers", urlPatterns = {"/home", "/home/view-detail-product"})
public class HomeControllers extends HttpServlet {

    private static final int HOME_SECTION_LIMIT = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/home/view-detail-product".equals(servletPath)) {
            showProductDetail(request, response);
            return;
        }
        showHome(request, response);
    }

    private void showHome(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            CategoryDAO categoryDAO = new CategoryDAO();
            ProductDAO productDAO = new ProductDAO();
            CommentDAO commentDAO = new CommentDAO();

            List<Category> categories = categoryDAO.getAllCategories();
            List<Product> latestProducts = productDAO.getLatestProducts(HOME_SECTION_LIMIT);
            List<Product> tops = productDAO.getProductsByCategoryName("Tops & Tees", HOME_SECTION_LIMIT);
            List<Product> outerwear = productDAO.getProductsByCategoryName("Outerwear", HOME_SECTION_LIMIT);
            List<Product> accessories = productDAO.getProductsByCategoryName("Accessories", HOME_SECTION_LIMIT);

            request.setAttribute("categories", categories);
            request.setAttribute("newArrivals", latestProducts);
            request.setAttribute("tops", tops);
            request.setAttribute("outerwear", outerwear);
            request.setAttribute("accessories", accessories);
            request.setAttribute("productDAO", productDAO);
            request.setAttribute("wishlistProductIds", getWishlistProductIds(request));
            request.setAttribute("cartCount", getCartCount(request));

            // Gom productId từ tất cả các section để lấy rating trong 1 lần query
            List<String> allProductIds = new ArrayList<>();
            allProductIds.addAll(collectProductIds(latestProducts));
            allProductIds.addAll(collectProductIds(tops));
            allProductIds.addAll(collectProductIds(outerwear));
            allProductIds.addAll(collectProductIds(accessories));

            Map<String, double[]> ratingMap = commentDAO.getRatingSummaryMap(allProductIds);
            request.setAttribute("ratingMap", ratingMap);

            String contentPage = "/Pages/Guest/Home/Content/Content.jsp";
            request.setAttribute("contentPage", contentPage);
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
        } catch (Exception e) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<h1>LỖI SERVER TẠI TRANG CHỦ:</h1>");
            response.getWriter().println("<p style='color:red;'>" + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }

    private void showProductDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductDAO productDAO = new ProductDAO();
        CommentDAO commentDAO = new CommentDAO();
        String productId = request.getParameter("productId");
        Product product = productDAO.getProductById(productId);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        List<Product> relatedProducts = productDAO.getProductsByCategoryName(product.getCategoryName(), 4);

        request.setAttribute("product", product);
        request.setAttribute("displayPrice", productDAO.getDisplayPrice(product));
        request.setAttribute("relatedProducts", relatedProducts);
        request.setAttribute("productDAO", productDAO);
        request.setAttribute("wishlistProductIds", getWishlistProductIds(request));
        request.setAttribute("cartCount", getCartCount(request));

        // Rating của sản phẩm đang xem
        double[] ratingSummary = commentDAO.getRatingSummary(productId);
        request.setAttribute("ratingSummary", ratingSummary);

        // Rating của các sản phẩm liên quan
        List<String> relatedIds = collectProductIds(relatedProducts);
        Map<String, double[]> ratingMap = commentDAO.getRatingSummaryMap(relatedIds);
        request.setAttribute("ratingMap", ratingMap);

        request.setAttribute("contentPage", "/Pages/Guest/Home/Content/ProductDetail.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Home Controller - Fashion Management System";
    }

    private int getCartCount(HttpServletRequest request) {
        // Check if user is logged in
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
        // Guest user - use session cart
        return SessionCartUtil.getCartCount(request);
    }

    private Account getLoggedInUser(HttpServletRequest request) {
        Object userObject = request.getSession(false) != null ? request.getSession(false).getAttribute("USER") : null;
        return userObject instanceof Account ? (Account) userObject : null;
    }

    private java.util.Set<String> getWishlistProductIds(HttpServletRequest request) {
        Account user = getLoggedInUser(request);
        if (user == null) {
            return new java.util.LinkedHashSet<>();
        }
        return new WishlistDAO().getWishlistProductIdsByAccountId(user.getAccountId());
    }

    private List<String> collectProductIds(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        return products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
    }
}