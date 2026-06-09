package Controllers;

import DALs.CategoryDAO;
import DALs.FavoriteProductDAO;
import DALs.ProductDAO;
import Models.Category;
import Models.Product;
import Models.Account;
import Utils.SessionCartUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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

            List<Category> categories = categoryDAO.getAllCategories();
            List<Product> latestProducts = productDAO.getLatestProducts(HOME_SECTION_LIMIT);

            request.setAttribute("categories", categories);
            request.setAttribute("newArrivals", latestProducts);
            request.setAttribute("tops", productDAO.getProductsByCategoryName("Tops & Tees", HOME_SECTION_LIMIT));
            request.setAttribute("outerwear", productDAO.getProductsByCategoryName("Outerwear", HOME_SECTION_LIMIT));
            request.setAttribute("accessories", productDAO.getProductsByCategoryName("Accessories", HOME_SECTION_LIMIT));
            request.setAttribute("productDAO", productDAO);
            request.setAttribute("wishlistProductIds", getFavoriteProductIds(request));
            request.setAttribute("cartCount", SessionCartUtil.getCartCount(request));

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
        String productId = request.getParameter("productId");
        Product product = productDAO.getProductById(productId);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        request.setAttribute("product", product);
        request.setAttribute("displayPrice", productDAO.getDisplayPrice(product));
        request.setAttribute("relatedProducts", productDAO.getProductsByCategoryName(product.getCategoryName(), 4));
        request.setAttribute("productDAO", productDAO);
        request.setAttribute("wishlistProductIds", getFavoriteProductIds(request));
        request.setAttribute("cartCount", SessionCartUtil.getCartCount(request));
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

    private java.util.Set<String> getFavoriteProductIds(HttpServletRequest request) {
        Object userObject = request.getSession(false) != null ? request.getSession(false).getAttribute("USER") : null;
        if (!(userObject instanceof Account)) {
            return new java.util.LinkedHashSet<>();
        }
        Account user = (Account) userObject;
        return new FavoriteProductDAO().getFavoriteProductIdsByAccountId(user.getAccountId());
    }
}
