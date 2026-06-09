package Controllers;

import DALs.CategoryDAO;
import DALs.ColorDAO;
import DALs.ProductDAO;
import DALs.ProductImageDAO;
import DALs.ProductVariantDAO;
import DALs.SizeDAO;
import Models.Account;
import Models.Category;
import Models.Color;
import Models.Product;
import Models.ProductVariant;
import Models.Size;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "ProductManagementServlet", urlPatterns = {"/admin/products", "/assets/product-images/*"})
@MultipartConfig
public class ProductManagementServlet extends HttpServlet {

    private static final List<String> VALID_STATUSES = Arrays.asList("Available", "OutOfStock", "Inactive");
    private static final int PRODUCT_PAGE_SIZE = 8;
    private static final int MANAGEMENT_PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action") != null
                ? request.getParameter("action") : "list";

        if (isProductImageRequest(request)) {
            serveProductImage(request, response);
            return;
        }

        switch (action) {
            case "create":
                showCreateForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                showDeleteForm(request, response);
                break;
            case "sizesByCategory":
                writeSizesByCategoryJson(request, response);
                break;
            case "createCategory":
                showCategoryForm(request, response, new Category(), "createCategory", "Thêm nhóm sản phẩm");
                break;
            case "editCategory":
                showEditCategoryForm(request, response);
                break;
            case "deleteCategory":
                showDeleteCategoryForm(request, response);
                break;
            case "createColor":
                showColorForm(request, response, new Color(), "createColor", "Thêm màu sắc");
                break;
            case "editColor":
                showEditColorForm(request, response);
                break;
            case "deleteColor":
                showDeleteColorForm(request, response);
                break;
            case "createSize":
                showSizeForm(request, response, new Size(), "createSize", "Thêm kích cỡ");
                break;
            case "editSize":
                showEditSizeForm(request, response);
                break;
            case "deleteSize":
                showDeleteSizeForm(request, response);
                break;
            default:
                showProductList(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action") != null
                ? request.getParameter("action") : "";

        switch (action) {
            case "create":
                createProduct(request, response);
                break;
            case "edit":
                updateProduct(request, response);
                break;
            case "delete":
                deleteProduct(request, response);
                break;
            case "createCategory":
                createCategory(request, response);
                break;
            case "editCategory":
                updateCategory(request, response);
                break;
            case "deleteCategory":
                deleteCategory(request, response);
                break;
            case "createColor":
                createColor(request, response);
                break;
            case "editColor":
                updateColor(request, response);
                break;
            case "deleteColor":
                deleteColor(request, response);
                break;
            case "createSize":
                createSize(request, response);
                break;
            case "editSize":
                updateSize(request, response);
                break;
            case "deleteSize":
                deleteSize(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/products");
                break;
        }
    }

    private void showProductList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        ColorDAO colorDAO = new ColorDAO();
        SizeDAO sizeDAO = new SizeDAO();
        if (!productDAO.isDatabaseReady()) {
            request.setAttribute("error", "Không thể kết nối cơ sở dữ liệu. Vui lòng kiểm tra lại cấu hình DB.");
        }

        List<Product> allProducts = productDAO.getAllProducts();
        List<Category> allCategories = categoryDAO.getAllCategories();
        List<Color> allColors = colorDAO.getAllColors();
        List<Size> allSizes = sizeDAO.getAllSizes();
        String keyword = trim(request.getParameter("keyword"));
        String statusFilter = trim(request.getParameter("statusFilter"));
        String categoryFilter = trim(request.getParameter("categoryFilter"));
        String activeTab = trim(request.getParameter("tab"));
        if (activeTab == null || activeTab.isBlank()) {
            activeTab = "products";
        }
        int currentPage = parsePositiveInt(request.getParameter("page"), 1);

        if (keyword != null && !keyword.isBlank()) {
            String normalizedKeyword = keyword.toLowerCase();
            allProducts.removeIf(product -> (product.getName() == null || !product.getName().toLowerCase().contains(normalizedKeyword))
                    && (product.getDescription() == null || !product.getDescription().toLowerCase().contains(normalizedKeyword))
                    && (product.getCategoryName() == null || !product.getCategoryName().toLowerCase().contains(normalizedKeyword)));
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            allProducts.removeIf(product -> product.getStatus() == null || !statusFilter.equalsIgnoreCase(product.getStatus()));
        }
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            allProducts.removeIf(product -> product.getCategoryId() == null || !categoryFilter.equals(product.getCategoryId()));
        }

        PageSlice<Product> productPage = paginate(allProducts, currentPage, PRODUCT_PAGE_SIZE);
        PageSlice<Category> categoryPage = paginate(allCategories, currentPage, MANAGEMENT_PAGE_SIZE);
        PageSlice<Color> colorPage = paginate(allColors, currentPage, MANAGEMENT_PAGE_SIZE);
        PageSlice<Size> sizePage = paginate(allSizes, currentPage, MANAGEMENT_PAGE_SIZE);

        PageSlice<?> activePage = switch (activeTab) {
            case "categories" -> categoryPage;
            case "colors" -> colorPage;
            case "sizes" -> sizePage;
            default -> productPage;
        };

        request.setAttribute("products", productPage.items());
        request.setAttribute("totalProducts", allProducts.size());
        request.setAttribute("categoryItems", categoryPage.items());
        request.setAttribute("totalCategories", allCategories.size());
        request.setAttribute("allCategoryItems", allCategories);
        request.setAttribute("colorItems", colorPage.items());
        request.setAttribute("totalColors", allColors.size());
        request.setAttribute("sizeItems", sizePage.items());
        request.setAttribute("totalSizes", allSizes.size());
        request.setAttribute("currentPage", activePage.currentPage());
        request.setAttribute("totalPages", activePage.totalPages());
        request.setAttribute("productQuery", buildProductManagementQuery(request));
        request.getRequestDispatcher("/views/pages/productManagement/listProduct.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Product product = new Product();
        loadReferenceData(request, null);
        request.setAttribute("product", product);
        request.setAttribute("formAction", "create");
        request.setAttribute("pageTitle", "Thêm sản phẩm");
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productId = request.getParameter("id");
        Product product = new ProductDAO().getProductById(productId);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Product-not-found&messageType=error");
            return;
        }

        loadReferenceData(request, product.getCategoryId());
        request.setAttribute("product", product);
        request.setAttribute("formAction", "edit");
        request.setAttribute("pageTitle", "Cập nhật sản phẩm");
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }

    private void showDeleteForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productId = request.getParameter("id");
        Product product = new ProductDAO().getProductById(productId);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Product not found&messageType=error");
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/views/pages/productManagement/deleteProduct.jsp").forward(request, response);
    }

    private void showEditCategoryForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Category category = new CategoryDAO().getCategoryById(request.getParameter("id"));
        if (category == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=categories&message=Category-not-found&messageType=error");
            return;
        }
        showCategoryForm(request, response, category, "editCategory", "Cập nhật nhóm sản phẩm");
    }

    private void showDeleteCategoryForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Category category = new CategoryDAO().getCategoryById(request.getParameter("id"));
        if (category == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=categories&message=Category not found&messageType=error");
            return;
        }
        request.setAttribute("category", category);
        request.getRequestDispatcher("/views/pages/productManagement/deleteCategory.jsp").forward(request, response);
    }

    private void showCategoryForm(HttpServletRequest request, HttpServletResponse response, Category category, String formAction, String pageTitle) throws ServletException, IOException {
        request.setAttribute("category", category);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/views/pages/productManagement/categoryForm.jsp").forward(request, response);
    }

    private void showEditColorForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Color color = new ColorDAO().getColorById(request.getParameter("id"));
        if (color == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=colors&message=Color-not-found&messageType=error");
            return;
        }
        showColorForm(request, response, color, "editColor", "Cập nhật màu sắc");
    }

    private void showDeleteColorForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Color color = new ColorDAO().getColorById(request.getParameter("id"));
        if (color == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=colors&message=Color-not-found&messageType=error");
            return;
        }
        request.setAttribute("color", color);
        request.getRequestDispatcher("/views/pages/productManagement/deleteColor.jsp").forward(request, response);
    }

    private void showColorForm(HttpServletRequest request, HttpServletResponse response, Color color, String formAction, String pageTitle) throws ServletException, IOException {
        request.setAttribute("color", color);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/views/pages/productManagement/colorForm.jsp").forward(request, response);
    }

    private void showEditSizeForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Size size = new SizeDAO().getSizeById(request.getParameter("id"));
        if (size == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=sizes&message=Size-not-found&messageType=error");
            return;
        }
        showSizeForm(request, response, size, "editSize", "Cập nhật kích cỡ");
    }

    private void showDeleteSizeForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Size size = new SizeDAO().getSizeById(request.getParameter("id"));
        if (size == null) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=sizes&message=Size not found&messageType=error");
            return;
        }
        request.setAttribute("size", size);
        request.getRequestDispatcher("/views/pages/productManagement/deleteSize.jsp").forward(request, response);
    }

    private void showSizeForm(HttpServletRequest request, HttpServletResponse response, Size size, String formAction, String pageTitle) throws ServletException, IOException {
        request.setAttribute("size", size);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("categories", new CategoryDAO().getAllCategories());
        request.getRequestDispatcher("/views/pages/productManagement/sizeForm.jsp").forward(request, response);
    }

    private void createProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Product product = buildProductFromRequest(request, false);
        String errorMessage = validateProduct(product);
        if (errorMessage != null) {
            loadReferenceData(request, product.getCategoryId());
            request.setAttribute("product", product);
            request.setAttribute("formAction", "create");
            request.setAttribute("pageTitle", "Thêm sản phẩm");
            request.setAttribute("error", errorMessage);
            request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
            return;
        }

        boolean created = new ProductDAO().createProduct(product);
        if (created && persistProductExtras(product, true)) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Product-created-successfully&messageType=success");
        } else {
            loadReferenceData(request, product.getCategoryId());
            request.setAttribute("product", product);
            request.setAttribute("formAction", "create");
            request.setAttribute("pageTitle", "Add Product");
            request.setAttribute("error", "Unable-to-create-product");
            request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Product product = buildProductFromRequest(request, true);
        String errorMessage = validateProduct(product);
        if (errorMessage != null) {
            loadReferenceData(request, product.getCategoryId());
            request.setAttribute("product", product);
            request.setAttribute("formAction", "edit");
            request.setAttribute("pageTitle", "Cập nhật sản phẩm");
            request.setAttribute("error", errorMessage);
            request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
            return;
        }

        boolean updated = new ProductDAO().updateProduct(product);
        if (updated && persistProductExtras(product, false)) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Product-updated-successfully&messageType=success");
        } else {
            loadReferenceData(request, product.getCategoryId());
            request.setAttribute("product", product);
            request.setAttribute("formAction", "edit");
            request.setAttribute("pageTitle", "Update Product");
            request.setAttribute("error", "Unable-to-update-product");
            request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = request.getParameter("productId");
        if (productId == null || productId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Invalid-product-id&messageType=error");
            return;
        }
        boolean deleted = new ProductDAO().deleteProduct(productId);
        response.sendRedirect(request.getContextPath() + (deleted
                ? "/admin/products?message=Product-deleted-successfully&messageType=success"
                : "/admin/products?message=Unable-to-delete-product-in-use&messageType=error"));
    }

    private void createCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Category category = buildCategoryFromRequest(request, false);
        String errorMessage = validateCategory(category);
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            showCategoryForm(request, response, category, "createCategory", "Thêm nhóm sản phẩm");
            return;
        }
        boolean created = new CategoryDAO().createCategory(category);
        if (created) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=categories&message=Category-created-successfully&messageType=success");
        } else {
            request.setAttribute("error", "Unable-to-create-category");
            showCategoryForm(request, response, category, "createCategory", "Add Category");
        }
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Category category = buildCategoryFromRequest(request, true);
        String errorMessage = validateCategory(category);
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            showCategoryForm(request, response, category, "editCategory", "Cập nhật nhóm sản phẩm");
            return;
        }
        boolean updated = new CategoryDAO().updateCategory(category);
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=categories&message=Category-updated-successfully&messageType=success");
        } else {
            request.setAttribute("error", "Unable-to-update-category");
            showCategoryForm(request, response, category, "editCategory", "Update Category");
        }
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String categoryId = request.getParameter("categoryId");
        if (categoryId == null || categoryId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=categories&message=Invalid-category-id&messageType=error");
            return;
        }
        boolean deleted = new CategoryDAO().deleteCategory(categoryId);
        response.sendRedirect(request.getContextPath() + (deleted
                ? "/admin/products?tab=categories&message=Category-deleted-successfully&messageType=success"
                : "/admin/products?tab=categories&message=Unable-to-delete-category-in-use&messageType=error"));
    }

    private void createColor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Color color = buildColorFromRequest(request, false);
        String errorMessage = validateColor(color);
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            showColorForm(request, response, color, "createColor", "Thêm màu sắc");
            return;
        }
        boolean created = new ColorDAO().createColor(color);
        if (created) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=colors&message=Color-created-successfully&messageType=success");
        } else {
            request.setAttribute("error", "Unable-to-create-color");
            showColorForm(request, response, color, "createColor", "Add Color");
        }
    }

    private void updateColor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Color color = buildColorFromRequest(request, true);
        String errorMessage = validateColor(color);
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            showColorForm(request, response, color, "editColor", "Cập nhật màu sắc");
            return;
        }
        boolean updated = new ColorDAO().updateColor(color);
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=colors&message=Color-updated-successfully&messageType=success");
        } else {
            request.setAttribute("error", "Unable-to-update-color");
            showColorForm(request, response, color, "editColor", "Update Color");
        }
    }

    private void deleteColor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String colorId = request.getParameter("colorId");
        if (colorId == null || colorId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=colors&message=Invalid-color-id&messageType=error");
            return;
        }
        boolean deleted = new ColorDAO().deleteColor(colorId);
        response.sendRedirect(request.getContextPath() + (deleted
                ? "/admin/products?tab=colors&message=Color-deleted-successfully&messageType=success"
                : "/admin/products?tab=colors&message=Unable-to-delete-color-in-use&messageType=error"));
    }

    private void createSize(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Size size = buildSizeFromRequest(request, false);
        String errorMessage = validateSize(size);
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            showSizeForm(request, response, size, "createSize", "Thêm kích cỡ");
            return;
        }
        boolean created = new SizeDAO().createSize(size);
        if (created) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=sizes&message=Size-created-successfully&messageType=success");
        } else {
            request.setAttribute("error", "Unable-to-create-size");
            showSizeForm(request, response, size, "createSize", "Add Size");
        }
    }

    private void updateSize(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Size size = buildSizeFromRequest(request, true);
        String errorMessage = validateSize(size);
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            showSizeForm(request, response, size, "editSize", "Cập nhật kích cỡ");
            return;
        }
        boolean updated = new SizeDAO().updateSize(size);
        if (updated) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=sizes&message=Size-updated-successfully&messageType=success");
        } else {
            request.setAttribute("error", "Unable-to-update-size");
            showSizeForm(request, response, size, "editSize", "Update Size");
        }
    }

    private void deleteSize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sizeId = request.getParameter("sizeId");
        if (sizeId == null || sizeId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/admin/products?tab=sizes&message=Invalid-size-id&messageType=error");
            return;
        }
        boolean deleted = new SizeDAO().deleteSize(sizeId);
        response.sendRedirect(request.getContextPath() + (deleted
                ? "/admin/products?tab=sizes&message=Size-deleted-successfully&messageType=success"
                : "/admin/products?tab=sizes&message=Unable-to-delete-size-in-use&messageType=error"));
    }

    private Product buildProductFromRequest(HttpServletRequest request, boolean includeId) throws IOException, ServletException {
        Product product = new Product();
        if (includeId) product.setProductId(trim(request.getParameter("productId")));
        product.setCategoryId(trim(request.getParameter("categoryId")));
        product.setName(trim(request.getParameter("name")));
        product.setDescription(trim(request.getParameter("description")));
        product.setStatus(trim(request.getParameter("status")));
        product.setPrimaryImageUrl(trim(request.getParameter("existingImageUrl")));

        String priceValue = normalizeCurrencyValue(trim(request.getParameter("basePrice")));
        try {
            product.setBasePrice(priceValue != null ? new BigDecimal(priceValue) : null);
        } catch (NumberFormatException ex) {
            product.setBasePrice(null);
        }

        product.setVariants(readVariantsFromRequest(request));
        hydrateVariantSummaries(product);

        Part imagePart = getImagePartSafely(request);
        if (imagePart != null && imagePart.getSize() > 0) {
            String uploadedImageUrl = saveImageFile(request, imagePart);
            if (uploadedImageUrl != null) product.setPrimaryImageUrl(uploadedImageUrl);
        }
        return product;
    }

    private Category buildCategoryFromRequest(HttpServletRequest request, boolean includeId) {
        Category category = new Category();
        if (includeId) category.setCategoryId(trim(request.getParameter("categoryId")));
        category.setName(trim(request.getParameter("name")));
        category.setDescription(trim(request.getParameter("description")));
        return category;
    }

    private Color buildColorFromRequest(HttpServletRequest request, boolean includeId) {
        Color color = new Color();
        if (includeId) color.setColorId(trim(request.getParameter("colorId")));
        color.setColorName(trim(request.getParameter("colorName")));
        color.setHexCode(normalizeHex(trim(request.getParameter("hexCode"))));
        return color;
    }

    private Size buildSizeFromRequest(HttpServletRequest request, boolean includeId) {
        Size size = new Size();
        if (includeId) size.setSizeId(trim(request.getParameter("sizeId")));
        size.setSizeName(trim(request.getParameter("sizeName")));
        size.setCategoryId(trim(request.getParameter("categoryId")));
        return size;
    }

    private String validateProduct(Product product) {
        if (product.getCategoryId() == null || product.getCategoryId().isBlank()) return "Vui lòng chọn nhóm sản phẩm.";
        if (product.getName() == null || product.getName().isBlank()) return "Vui lòng nhập tên sản phẩm.";
        if (product.getName().length() > 200) return "Tên sản phẩm không được vượt quá 200 ký tự.";
        if (product.getBasePrice() == null) return "Vui lòng nhập giá bán hợp lệ.";
        if (product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) return "Giá bán phải lớn hơn 0.";
        if (product.getStatus() == null || !VALID_STATUSES.contains(product.getStatus())) return "Trạng thái không hợp lệ.";
        if (product.getVariants() == null || product.getVariants().isEmpty()) return "Vui lòng thêm ít nhất một lựa chọn bán hàng cho sản phẩm.";
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getSizeId() == null || variant.getSizeId().isBlank()) return "Mỗi lựa chọn phải có kích cỡ.";
            if (variant.getColorId() == null || variant.getColorId().isBlank()) return "Mỗi lựa chọn phải có màu sắc.";
            if (variant.getSku() == null || variant.getSku().isBlank()) return "Mỗi lựa chọn phải có mã hàng.";
            if (variant.getStockQty() < 0) return "Số lượng tồn không được âm.";
            if (variant.getPriceOverride() != null && variant.getPriceOverride().compareTo(BigDecimal.ZERO) < 0) return "Giá bán riêng không được âm.";
        }
        return null;
    }

    private String validateCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) return "Vui lòng nhập tên nhóm sản phẩm.";
        if (category.getName().length() > 200) return "Tên nhóm sản phẩm không được vượt quá 200 ký tự.";
        return null;
    }

    private String validateColor(Color color) {
        if (color.getColorName() == null || color.getColorName().isBlank()) return "Vui lòng nhập tên màu sắc.";
        if (color.getColorName().length() > 100) return "Tên màu sắc không được vượt quá 100 ký tự.";
        if (color.getHexCode() == null || !color.getHexCode().matches("^#[0-9A-Fa-f]{6}$")) return "Vui lòng chọn màu hợp lệ.";
        return null;
    }

    private String validateSize(Size size) {
        if (size.getSizeName() == null || size.getSizeName().isBlank()) return "Vui lòng nhập tên kích cỡ.";
        if (size.getSizeName().length() > 50) return "Tên kích cỡ không được vượt quá 50 ký tự.";
        if (size.getCategoryId() == null || size.getCategoryId().isBlank()) return "Vui lòng chọn nhóm sản phẩm cho kích cỡ này.";
        return null;
    }

    private void loadReferenceData(HttpServletRequest request, String categoryId) {
        CategoryDAO categoryDAO = new CategoryDAO();
        List<Category> categories = categoryDAO.getAllCategories();
        request.setAttribute("categories", categories);
        request.setAttribute("statuses", VALID_STATUSES);
        request.setAttribute("sizes", new SizeDAO().getSizesByCategoryId(categoryId));
        request.setAttribute("allSizes", new SizeDAO().getAllSizes());
        request.setAttribute("colors", new ColorDAO().getAllColors());
    }

    private void writeSizesByCategoryJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String categoryId = trim(request.getParameter("categoryId"));
        List<Size> sizes = new SizeDAO().getSizesByCategoryId(categoryId);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < sizes.size(); i++) {
            Size size = sizes.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"sizeId\":\"")
                    .append(escapeJson(size.getSizeId()))
                    .append("\",\"sizeName\":\"")
                    .append(escapeJson(size.getSizeName()))
                    .append("\"}");
        }
        json.append("]");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean persistProductExtras(Product product, boolean deleteProductOnFailure) {
        if (product == null || product.getProductId() == null || product.getProductId().isBlank()) return false;
        ProductImageDAO imageDAO = new ProductImageDAO();
        ProductVariantDAO variantDAO = new ProductVariantDAO();
        ProductDAO productDAO = new ProductDAO();
        boolean imageSaved = true;
        if (product.getPrimaryImageUrl() != null && !product.getPrimaryImageUrl().isBlank()) {
            imageSaved = imageDAO.upsertPrimaryImage(product.getProductId(), product.getPrimaryImageUrl());
        }
        boolean variantsSaved = variantDAO.replaceVariants(product.getProductId(), product.getVariants());
        if (deleteProductOnFailure && (!imageSaved || !variantsSaved)) {
            productDAO.deleteProduct(product.getProductId());
        }
        return imageSaved && variantsSaved;
    }

    private List<ProductVariant> readVariantsFromRequest(HttpServletRequest request) {
        String[] sizeIds = request.getParameterValues("variantSizeId");
        String[] colorIds = request.getParameterValues("variantColorId");
        String[] skus = request.getParameterValues("variantSku");
        String[] stockQtys = request.getParameterValues("variantStockQty");
        String[] priceOverrides = request.getParameterValues("variantPriceOverride");
        String[] enableds = request.getParameterValues("variantEnabled");

        if ((sizeIds == null || sizeIds.length == 0) && (colorIds == null || colorIds.length == 0)) {
            String[] selectedSizeIds = request.getParameterValues("selectedSizeId");
            String[] selectedColorIds = request.getParameterValues("selectedColorId");
            if (selectedSizeIds != null && selectedColorIds != null && selectedSizeIds.length > 0 && selectedColorIds.length > 0) {
                List<String> sizeIdList = new ArrayList<>();
                List<String> colorIdList = new ArrayList<>();
                List<String> skuList = new ArrayList<>();
                List<String> stockList = new ArrayList<>();
                List<String> priceList = new ArrayList<>();
                List<String> enabledList = new ArrayList<>();

                for (String sizeId : selectedSizeIds) {
                    for (String colorId : selectedColorIds) {
                        sizeIdList.add(sizeId);
                        colorIdList.add(colorId);
                        skuList.add(null);
                        stockList.add("0");
                        priceList.add(null);
                        enabledList.add("true");
                    }
                }

                sizeIds = sizeIdList.toArray(new String[0]);
                colorIds = colorIdList.toArray(new String[0]);
                skus = skuList.toArray(new String[0]);
                stockQtys = stockList.toArray(new String[0]);
                priceOverrides = priceList.toArray(new String[0]);
                enableds = enabledList.toArray(new String[0]);
            }
        }

        int rowCount = maxLength(sizeIds, colorIds, skus, stockQtys, priceOverrides, enableds);
        List<ProductVariant> variants = new ArrayList<>();
        Map<String, ProductVariant> dedup = new LinkedHashMap<>();

        for (int i = 0; i < rowCount; i++) {
            String enabled = getArrayValue(enableds, i);
            String sizeId = getArrayValue(sizeIds, i);
            String colorId = getArrayValue(colorIds, i);
            String sku = getArrayValue(skus, i);
            String stockQtyRaw = getArrayValue(stockQtys, i);
            String priceOverrideRaw = normalizeCurrencyValue(getArrayValue(priceOverrides, i));

            if ("false".equalsIgnoreCase(enabled) || isAllBlank(sizeId, colorId, sku, stockQtyRaw, priceOverrideRaw)) continue;

            ProductVariant variant = new ProductVariant();
            variant.setSizeId(sizeId);
            variant.setColorId(colorId);
            variant.setSku(sku);
            variant.setStockQty(parseInteger(stockQtyRaw));
            variant.setPriceOverride(parseBigDecimal(priceOverrideRaw));
            dedup.put((sizeId == null ? "" : sizeId) + "::" + (colorId == null ? "" : colorId), variant);
        }

        variants.addAll(dedup.values());
        return variants;
    }

    private void hydrateVariantSummaries(Product product) {
        List<String> sizeIds = new ArrayList<>();
        List<String> colorIds = new ArrayList<>();
        int totalStockQty = 0;
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getSizeId() != null && !sizeIds.contains(variant.getSizeId())) sizeIds.add(variant.getSizeId());
                if (variant.getColorId() != null && !colorIds.contains(variant.getColorId())) colorIds.add(variant.getColorId());
                totalStockQty += Math.max(0, variant.getStockQty());
            }
        }
        product.setSizeIds(sizeIds);
        product.setColorIds(colorIds);
        product.setTotalStockQty(totalStockQty);
    }

    private int maxLength(String[]... arrays) {
        int max = 0;
        for (String[] array : arrays) if (array != null && array.length > max) max = array.length;
        return max;
    }

    private String getArrayValue(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) return null;
        return trim(values[index]);
    }

    private boolean isAllBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return false;
        return true;
    }

    private int parseInteger(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return 0;
        try { return Integer.parseInt(rawValue.trim()); } catch (NumberFormatException ex) { return -1; }
    }

    private BigDecimal parseBigDecimal(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return null;
        try { return new BigDecimal(rawValue); } catch (NumberFormatException ex) { return null; }
    }

    private String normalizeCurrencyValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return null;
        return rawValue.replace("đ", "").replace("₫", "").replace("VND", "").replace("vnd", "").replace(".", "").replace(",", "").replaceAll("\\s+", "").trim();
    }

    private String normalizeHex(String rawHex) {
        if (rawHex == null || rawHex.isBlank()) return null;
        String hex = rawHex.trim().toUpperCase();
        if (!hex.startsWith("#")) hex = "#" + hex;
        return hex;
    }

    private Part getImagePartSafely(HttpServletRequest request) {
        try { return request.getPart("productImage"); } catch (Exception ex) { return null; }
    }

    private String saveImageFile(HttpServletRequest request, Part imagePart) throws IOException {
        String rawSubmittedFileName = imagePart.getSubmittedFileName();
        if (rawSubmittedFileName == null || rawSubmittedFileName.isBlank()) return null;
        String submittedFileName = Paths.get(rawSubmittedFileName).getFileName().toString();
        if (submittedFileName.isBlank()) return null;
        String extension = "";
        int dotIndex = submittedFileName.lastIndexOf('.');
        if (dotIndex >= 0) extension = submittedFileName.substring(dotIndex);
        String storedFileName = "product-" + UUID.randomUUID().toString().replace("-", "") + extension;
        Path uploadDir = resolveUploadDirectory();
        Files.createDirectories(uploadDir);
        Path destination = uploadDir.resolve(storedFileName);
        imagePart.write(destination.toAbsolutePath().toString());
        return request.getContextPath() + "/assets/product-images/" + storedFileName;
    }

    private Path resolveUploadDirectory() {
        String realPath = requestSafeRealPath();
        if (realPath != null && !realPath.isBlank()) {
            Path deployedAssetsDir = Paths.get(realPath, "assets", "product-images");
            Path parent = deployedAssetsDir.getParent();
            if (parent != null) {
                return deployedAssetsDir;
            }
        }
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot != null && !projectRoot.isBlank()) {
            Path projectAssetsDir = Paths.get(projectRoot, "src", "main", "webapp", "assets", "product-images");
            Path parent = projectAssetsDir.getParent();
            if (parent != null && Files.exists(parent)) {
                return projectAssetsDir;
            }
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "fashion-management-system", "product-images");
    }

    private String requestSafeRealPath() {
        try { return getServletContext().getRealPath("/"); } catch (Exception ex) { return null; }
    }

    private boolean isProductImageRequest(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        return "/assets/product-images".equals(servletPath)
                || (servletPath != null && servletPath.startsWith("/assets/product-images"))
                || ("/admin/products".equals(servletPath) && pathInfo != null && pathInfo.startsWith("/assets/product-images/"));
    }

    private <T> PageSlice<T> paginate(List<T> items, int requestedPage, int pageSize) {
        int totalItems = items.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.min(Math.max(1, requestedPage), totalPages);
        int fromIndex = Math.max(0, (currentPage - 1) * pageSize);
        int toIndex = Math.min(totalItems, fromIndex + pageSize);
        List<T> pageItems = items.subList(fromIndex, toIndex);
        return new PageSlice<>(pageItems, currentPage, totalPages, totalItems);
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String buildProductManagementQuery(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .filter(entry -> !"page".equals(entry.getKey()))
                .flatMap(entry -> Arrays.stream(entry.getValue()).map(value -> entry.getKey() + "=" + value))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private record PageSlice<T>(List<T> items, int currentPage, int totalPages, int totalItems) {
    }

    private void serveProductImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String imageName = extractRequestedImageName(request);
        if (imageName == null || imageName.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path imagePath = resolveUploadDirectory().resolve(imageName).normalize();
        if (!Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(imagePath);
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(imagePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private String extractRequestedImageName(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.isBlank()) {
            path = request.getServletPath();
        }
        if (path == null || path.isBlank()) {
            return null;
        }

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex < 0 || lastSlashIndex == path.length() - 1) {
            return null;
        }
        return Paths.get(path.substring(lastSlashIndex + 1)).getFileName().toString();
    }

    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object userObject = request.getSession(false) != null ? request.getSession(false).getAttribute("USER") : null;
        if (!(userObject instanceof Account)) {
            System.out.println("[ProductManagementServlet] Access denied: USER session missing or invalid");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }
        Account account = (Account) userObject;
        System.out.println("[ProductManagementServlet] Session role = " + account.getRole() + ", email = " + account.getEmail());
        if (account.getRole() == null || !account.getRole().equalsIgnoreCase("Admin")) {
            System.out.println("[ProductManagementServlet] Access denied: non-admin user redirected to home");
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }
        return true;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
