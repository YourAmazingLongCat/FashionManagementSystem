package Controllers;

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
import Services.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;


@WebServlet(name = "ProductManagementServlet", urlPatterns = {"/staff/products", "/assets/product-images/*"})
@MultipartConfig
public class ProductManagementServlet extends HttpServlet {

    // Valid product statuses
    private static final List<String> VALID_STATUSES = Arrays.asList("Available", "Inactive", "OutOfStock");
    private static final int DEFAULT_PAGE_SIZE = 8;

    private ProductService productService;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private ProductImageDAO imageDAO;
    private CategoryDAO categoryDAO;
    private ColorDAO colorDAO;
    private SizeDAO sizeDAO;

    @Override
    public void init() {
        productService = new ProductService();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        imageDAO = new ProductImageDAO();
        categoryDAO = new CategoryDAO();
        colorDAO = new ColorDAO();
        sizeDAO = new SizeDAO();
    }

    // ============ GET ============

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Serve product image files (URL /assets/product-images/xxx)
        if (isProductImageRequest(request)) {
            serveProductImage(request, response);
            return;
        }

        // Check staff permission
        if (!checkStaff(request, response)) return;

        String action = getAction(request);

        switch (action) {
            case "create":
                showCreateForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "manageVariants":
                showVariantManagement(request, response);
                break;
            case "delete":
                showDeleteForm(request, response);
                break;
            case "getProductJson":
                writeProductJson(request, response);
                break;
            case "getCategoryJson":
                writeCategoryJson(request, response);
                break;
            case "getColorJson":
                writeColorJson(request, response);
                break;
            case "getSizeJson":
                writeSizeJson(request, response);
                break;
            case "createCategory":
                showCategoryForm(request, response, new Category(), "createCategory", "Add Category");
                break;
            case "editCategory":
                showEditCategoryForm(request, response);
                break;
            case "deleteCategory":
                showDeleteCategoryForm(request, response);
                break;
            case "createColor":
                showColorForm(request, response, new Color(), "createColor", "Add Color");
                break;
            case "editColor":
                showEditColorForm(request, response);
                break;
            case "deleteColor":
                showDeleteColorForm(request, response);
                break;
            case "createSize":
                showSizeForm(request, response, new Size(), "createSize", "Add Size");
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

    // ============ POST ============

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkStaff(request, response)) return;

        String action = getAction(request);

        switch (action) {
            case "create":
                createProduct(request, response);
                break;
            case "edit":
                updateProduct(request, response);
                break;
            case "createVariant":
                createVariant(request, response);
                break;
            case "delete":
            case "deleteProduct":
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
                redirectToList(request, response);
                break;
        }
    }

    // ============ List page ============

    private void showVariantManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Product> products = productDAO.getAllProducts();
        String selectedProductId = getTrimmedParam(request, "productId", "");
        Product selectedProduct = findProduct(products, selectedProductId);
        String keyword = getTrimmedParam(request, "keyword", "");
        String categoryId = getTrimmedParam(request, "categoryId", "");
        List<ProductVariant> allVariants;
        boolean isFiltering = !keyword.isBlank() || !categoryId.isBlank();
        if (isFiltering) {
            allVariants = variantDAO.searchVariants(keyword, categoryId);
        } else {
            allVariants = variantDAO.getAllVariants();
        }
        int pageSize = 8;
        int currentPage = parsePositiveInt(request.getParameter("page"), 1);
        PageSlice<ProductVariant> variantPage = paginate(allVariants, currentPage, pageSize);

        request.setAttribute("products", products);
        request.setAttribute("variants", variantPage.items());
        request.setAttribute("totalVariants", allVariants.size());
        request.setAttribute("currentPage", variantPage.currentPage());
        request.setAttribute("totalPages", variantPage.totalPages());
        request.setAttribute("selectedProduct", selectedProduct);
        request.setAttribute("sizes", sizeDAO.getAllSizes());
        request.setAttribute("allSizes", sizeDAO.getAllSizes());
        request.setAttribute("colors", colorDAO.getAllColors());
        request.setAttribute("categories", categoryDAO.getAllCategories());
        request.setAttribute("selectedProductId", selectedProductId);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedCategoryId", categoryId);
        request.getRequestDispatcher("/views/pages/productManagement/manageProductVariants.jsp").forward(request, response);
    }

    private Product findProduct(List<Product> products, String productId) {
        if (products == null || productId == null || productId.isBlank()) return null;
        for (Product product : products) {
            if (productId.equals(product.getProductId())) return product;
        }
        return null;
    }

    private void createVariant(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productId = getTrimmedParam(request, "productId");
        String sizeId = getTrimmedParam(request, "sizeId");
        String colorId = getTrimmedParam(request, "colorId");
        Product product = productDAO.getProductById(productId);
        Part imagePart = getVariantImagePartSafely(request);

        String error = null;
        if (product == null) error = "Please choose a valid product.";
        else if (isBlank(sizeId)) error = "Please choose a size.";
        else if (isBlank(colorId)) error = "Please choose a color.";
        else if (imagePart == null || imagePart.getSize() <= 0) error = "Please choose an image for the variant.";

        BigDecimal priceOverride = parseBigDecimal(normalizeCurrencyValue(getTrimmedParam(request, "priceOverride")));
        if (error == null && priceOverride != null && priceOverride.compareTo(BigDecimal.ZERO) < 0) {
            error = "Variant price cannot be negative.";
        }

        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSizeId(sizeId);
        variant.setColorId(colorId);
        variant.setSku(getTrimmedParam(request, "sku"));
        variant.setPriceOverride(priceOverride);

        if (error == null && variantDAO.hasVariantCombination(productId, sizeId, colorId)) {
            error = "This size and color combination already exists for the selected product.";
        }

        if (error == null && variantDAO.createVariant(variant)) {
            String variantId = variantDAO.getLatestVariantId(variant);
            if (imagePart != null && imagePart.getSize() > 0 && variantId != null) {
                String imageUrl = saveImageFile(request, imagePart);
                if (imageUrl != null && !imageDAO.addVariantImage(productId, variantId, imageUrl)) {
                    System.out.println("Variant saved, but image metadata could not be saved for variant " + variantId);
                }
            }
            response.sendRedirect(request.getContextPath() + "/staff/products?action=manageVariants&productId="
                    + java.net.URLEncoder.encode(productId, "UTF-8") + "&message=Variant+created+successfully&messageType=success");
            return;
        }

        List<Product> products = productDAO.getAllProducts();
        Product selectedProduct = findProduct(products, productId);
        List<ProductVariant> allVariants = variantDAO.getAllVariants();
        PageSlice<ProductVariant> variantPage = paginate(allVariants, 1, 8);
        request.setAttribute("products", products);
        request.setAttribute("variants", variantPage.items());
        request.setAttribute("totalVariants", allVariants.size());
        request.setAttribute("currentPage", variantPage.currentPage());
        request.setAttribute("totalPages", variantPage.totalPages());
        request.setAttribute("selectedProduct", selectedProduct);
        request.setAttribute("sizes", sizeDAO.getAllSizes());
        request.setAttribute("allSizes", sizeDAO.getAllSizes());
        request.setAttribute("colors", colorDAO.getAllColors());
        request.setAttribute("selectedProductId", productId);
        request.setAttribute("formError", error == null ? "Unable to create variant." : error);
        request.getRequestDispatcher("/views/pages/productManagement/manageProductVariants.jsp").forward(request, response);
    }

    private void showProductList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!productService.isDatabaseReady()) {
            request.setAttribute("error", "Unable to connect to database. Please check DB configuration.");
        }

        String keyword = getTrimmedParam(request, "keyword");
        String statusFilter = getTrimmedParam(request, "statusFilter");
        String categoryFilter = getTrimmedParam(request, "categoryFilter");
        String activeTab = getTrimmedParam(request, "tab", "products");
        int currentPage = parsePositiveInt(request.getParameter("page"), 1);

        DALs.ProductDAO.ProductResult productResult = productService.getProducts(
                keyword, statusFilter, categoryFilter, currentPage, DEFAULT_PAGE_SIZE
        );

        // Load data for all tabs
        List<Category> allCategories = categoryDAO.getAllCategories();
        List<Color> allColors = colorDAO.getAllColors();
        List<Size> allSizes = sizeDAO.getAllSizes();

        // Paginate each tab
        PageSlice<Category> categoryPage = paginate(allCategories, currentPage, DEFAULT_PAGE_SIZE);
        PageSlice<Color> colorPage = paginate(allColors, currentPage, DEFAULT_PAGE_SIZE);
        PageSlice<Size> sizePage = paginate(allSizes, currentPage, DEFAULT_PAGE_SIZE);

        PageSlice<?> activePage;
        switch (activeTab) {
            case "categories": activePage = categoryPage; break;
            case "colors":     activePage = colorPage; break;
            case "sizes":      activePage = sizePage; break;
            default:
                activePage = new PageSlice<>(
                        productResult.getProducts(),
                        currentPage,
                        productResult.getTotalPages(DEFAULT_PAGE_SIZE),
                        productResult.getTotalCount()
                );
        }

        request.setAttribute("products", productResult.getProducts());
        request.setAttribute("totalProducts", productResult.getTotalCount());
        request.setAttribute("categoryItems", categoryPage.items());
        request.setAttribute("totalCategories", allCategories.size());
        request.setAttribute("allCategoryItems", allCategories);
        request.setAttribute("colorItems", colorPage.items());
        request.setAttribute("totalColors", allColors.size());
        request.setAttribute("sizeItems", sizePage.items());
        request.setAttribute("totalSizes", allSizes.size());
        request.setAttribute("currentPage", activePage.currentPage());
        request.setAttribute("totalPages", activePage.totalPages());
        request.setAttribute("productQuery", buildQuery(request));

        // Data for edit modal on this page
        request.setAttribute("categories", allCategories);
        request.setAttribute("colors", allColors);
        request.setAttribute("allSizes", allSizes);
        request.setAttribute("statuses", VALID_STATUSES);

        request.getRequestDispatcher("/views/pages/productManagement/listProduct.jsp").forward(request, response);
    }

    // ============ Product form ============

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("product", new Product());
        request.setAttribute("formAction", "create");
        request.setAttribute("pageTitle", "Add Product");
        loadReferenceData(request, null);
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productId = request.getParameter("id");
        Product product = productService.getProduct(productId);

        if (product == null) {
            redirectWithMessage(request, response, "Product not found", "error");
            return;
        }

        request.setAttribute("product", product);
        request.setAttribute("formAction", "edit");
        request.setAttribute("pageTitle", "Update Product");
        loadReferenceData(request, product.getCategoryId());
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }

    /**
     * Show form with success/error message, stay on productForm.
     * Used after create/update so the user stays on the same page.
     */
    private void showEditFormWithMessage(HttpServletRequest request, HttpServletResponse response,
                                        Product product, String formAction, String pageTitle,
                                        String message, String messageType)
            throws ServletException, IOException {
        request.setAttribute("product", product != null ? product : new Product());
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("success", message);
        request.setAttribute("messageType", messageType);
        loadReferenceData(request, product != null ? product.getCategoryId() : null);
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }

    private void showDeleteForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productId = request.getParameter("id");
        Product product = productService.getProduct(productId);

        if (product == null) {
            redirectWithMessage(request, response, "Product not found", "error");
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/views/pages/productManagement/deleteProduct.jsp").forward(request, response);
    }

    private void createProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Product product = buildProductFromRequest(request, false);

        String error = validateProduct(product, false);
        if (error != null) {
            forwardWithError(request, response, product, "create", "Add Product", error);
            return;
        }

        boolean ok = productService.createProduct(product);
        if (ok) {
            // Save done: go back to product list with alert
            redirectWithMessage(request, response, "Product created successfully", "success");
        } else {
            forwardWithError(request, response, product, "create", "Add Product", "Unable to create product");
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Product product = buildProductFromRequest(request, true);

        String error = validateProduct(product, true);
        if (error != null) {
            forwardWithError(request, response, product, "edit", "Update Product", error);
            return;
        }

        boolean ok = productService.updateProduct(product);
        if (ok) {
            // Save done: go back to product list with alert
            redirectWithMessage(request, response, "Product updated successfully", "success");
        } else {
            forwardWithError(request, response, product, "edit", "Update Product", "Unable to update product");
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = request.getParameter("productId");

        if (productId == null || productId.isBlank()) {
            sendDeleteResponse(request, response, false, "Invalid product id.");
            return;
        }

        ProductService.DeleteResult result = productService.deleteProduct(productId);
        sendDeleteResponse(request, response, result.success, result.message);
    }

    /**
     * Send delete result back to client. For AJAX calls -> JSON, otherwise
     * redirect with flash message so the list page can render it.
     */
    private void sendDeleteResponse(HttpServletRequest request, HttpServletResponse response,
                                    boolean ok, String message) throws IOException {
        if (isAjaxRequest(request)) {
            writeJsonResponse(response, ok, message);
        } else {
            redirectWithMessage(request, response, message, ok ? "success" : "error");
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        return xrw != null && xrw.equalsIgnoreCase("XMLHttpRequest");
    }

    // ============ Category form ============

    private void showEditCategoryForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryDAO.getCategoryById(request.getParameter("id"));
        if (category == null) {
            redirectWithTab(request, response, "categories", "Category not found", "error");
            return;
        }
        showCategoryForm(request, response, category, "editCategory", "Update Category");
    }

    private void showDeleteCategoryForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryDAO.getCategoryById(request.getParameter("id"));
        if (category == null) {
            redirectWithTab(request, response, "categories", "Category not found", "error");
            return;
        }
        request.setAttribute("category", category);
        request.getRequestDispatcher("/views/pages/productManagement/deleteCategory.jsp").forward(request, response);
    }

    private void showCategoryForm(HttpServletRequest request, HttpServletResponse response,
                                  Category category, String formAction, String pageTitle)
            throws ServletException, IOException {
        request.setAttribute("category", category);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/views/pages/productManagement/categoryForm.jsp").forward(request, response);
    }

    private void createCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = buildCategoryFromRequest(request);
        String error = validateCategory(category);
        if (error != null) {
            writeJsonResponse(response, false, error);
            return;
        }

        try {
            boolean ok = categoryDAO.createCategory(category);
            if (ok) {
                writeJsonResponse(response, true, "Category created successfully");
            } else {
                writeJsonResponse(response, false, "Unable to create category");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "category"));
        }
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = buildCategoryFromRequest(request);
        String error = validateCategory(category);
        if (error != null) {
            writeJsonResponse(response, false, error);
            return;
        }

        try {
            boolean ok = categoryDAO.updateCategory(category);
            if (ok) {
                writeJsonResponse(response, true, "Category updated successfully");
            } else {
                writeJsonResponse(response, false, "Unable to update category");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "category"));
        }
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String categoryId = request.getParameter("categoryId");
        if (categoryId == null || categoryId.isBlank()) {
            writeJsonResponse(response, false, "Invalid category id");
            return;
        }

        // Check restriction: is any product using this category?
        String conflict = checkCategoryInUse(categoryId);
        if (conflict != null) {
            writeJsonResponse(response, false, conflict);
            return;
        }

        try {
            boolean ok = categoryDAO.deleteCategory(categoryId);
            if (ok) {
                writeJsonResponse(response, true, "Category deleted successfully");
            } else {
                writeJsonResponse(response, false, "Unable to delete category");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "category"));
        }
    }

    // ============ Color form ============

    private void showEditColorForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = colorDAO.getColorById(request.getParameter("id"));
        if (color == null) {
            redirectWithTab(request, response, "colors", "Color not found", "error");
            return;
        }
        showColorForm(request, response, color, "editColor", "Update Color");
    }

    private void showDeleteColorForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = colorDAO.getColorById(request.getParameter("id"));
        if (color == null) {
            redirectWithTab(request, response, "colors", "Color not found", "error");
            return;
        }
        request.setAttribute("color", color);
        request.getRequestDispatcher("/views/pages/productManagement/deleteColor.jsp").forward(request, response);
    }

    private void showColorForm(HttpServletRequest request, HttpServletResponse response,
                              Color color, String formAction, String pageTitle)
            throws ServletException, IOException {
        request.setAttribute("color", color);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/views/pages/productManagement/colorForm.jsp").forward(request, response);
    }

    private void createColor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = buildColorFromRequest(request);
        String error = validateColor(color);
        if (error != null) {
            writeJsonResponse(response, false, error);
            return;
        }

        try {
            boolean ok = colorDAO.createColor(color);
            if (ok) {
                writeJsonResponse(response, true, "Color created successfully");
            } else {
                writeJsonResponse(response, false, "Unable to create color");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "color"));
        }
    }

    private void updateColor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = buildColorFromRequest(request);
        String error = validateColor(color);
        if (error != null) {
            writeJsonResponse(response, false, error);
            return;
        }

        try {
            boolean ok = colorDAO.updateColor(color);
            if (ok) {
                writeJsonResponse(response, true, "Color updated successfully");
            } else {
                writeJsonResponse(response, false, "Unable to update color");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "color"));
        }
    }

    private void deleteColor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String colorId = request.getParameter("colorId");
        if (colorId == null || colorId.isBlank()) {
            writeJsonResponse(response, false, "Invalid color id");
            return;
        }

        // Check restriction: is any variant using this color?
        String conflict = checkColorInUse(colorId);
        if (conflict != null) {
            writeJsonResponse(response, false, conflict);
            return;
        }

        try {
            boolean ok = colorDAO.deleteColor(colorId);
            if (ok) {
                writeJsonResponse(response, true, "Color deleted successfully");
            } else {
                writeJsonResponse(response, false, "Unable to delete color");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "color"));
        }
    }

    // ============ Size form ============

    private void showEditSizeForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = sizeDAO.getSizeById(request.getParameter("id"));
        if (size == null) {
            redirectWithTab(request, response, "sizes", "Size not found", "error");
            return;
        }
        showSizeForm(request, response, size, "editSize", "Update Size");
    }

    private void showDeleteSizeForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = sizeDAO.getSizeById(request.getParameter("id"));
        if (size == null) {
            redirectWithTab(request, response, "sizes", "Size not found", "error");
            return;
        }
        request.setAttribute("size", size);
        request.getRequestDispatcher("/views/pages/productManagement/deleteSize.jsp").forward(request, response);
    }

    private void showSizeForm(HttpServletRequest request, HttpServletResponse response,
                            Size size, String formAction, String pageTitle)
            throws ServletException, IOException {
        request.setAttribute("size", size);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher("/views/pages/productManagement/sizeForm.jsp").forward(request, response);
    }

    private void createSize(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = buildSizeFromRequest(request);
        String error = validateSize(size);
        if (error != null) {
            writeJsonResponse(response, false, error);
            return;
        }

        try {
            boolean ok = sizeDAO.createSize(size);
            if (ok) {
                writeJsonResponse(response, true, "Size created successfully");
            } else {
                writeJsonResponse(response, false, "Unable to create size");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "size"));
        }
    }

    private void updateSize(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = buildSizeFromRequest(request);
        String error = validateSize(size);
        if (error != null) {
            writeJsonResponse(response, false, error);
            return;
        }

        try {
            boolean ok = sizeDAO.updateSize(size);
            if (ok) {
                writeJsonResponse(response, true, "Size updated successfully");
            } else {
                writeJsonResponse(response, false, "Unable to update size");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "size"));
        }
    }

    private void deleteSize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sizeId = request.getParameter("sizeId");
        if (sizeId == null || sizeId.isBlank()) {
            writeJsonResponse(response, false, "Invalid size id");
            return;
        }

        // Check restriction: is any variant using this size?
        String conflict = checkSizeInUse(sizeId);
        if (conflict != null) {
            writeJsonResponse(response, false, conflict);
            return;
        }

        try {
            boolean ok = sizeDAO.deleteSize(sizeId);
            if (ok) {
                writeJsonResponse(response, true, "Size deleted successfully");
            } else {
                writeJsonResponse(response, false, "Unable to delete size");
            }
        } catch (java.sql.SQLException ex) {
            writeJsonResponse(response, false, translateDbError(ex, "size"));
        }
    }

    // ============ Build / Validate ============

    private Product buildProductFromRequest(HttpServletRequest request, boolean includeId) throws IOException, ServletException {
        Product product = new Product();

        if (includeId) {
            product.setProductId(getTrimmedParam(request, "productId"));
        }
        product.setCategoryId(getTrimmedParam(request, "categoryId"));
        product.setName(getTrimmedParam(request, "name"));
        product.setDescription(getTrimmedParam(request, "description"));
        product.setStatus(getTrimmedParam(request, "status"));
        product.setPrimaryImageUrl(getTrimmedParam(request, "existingImageUrl"));

        String priceValue = normalizeCurrencyValue(getTrimmedParam(request, "basePrice"));
        try {
            product.setBasePrice(priceValue != null ? new BigDecimal(priceValue) : null);
        } catch (NumberFormatException ex) {
            product.setBasePrice(null);
        }

        // Edit: keep old stockQty for existing variants (new variants = 0)
        Map<String, int[]> existingStockByVariantId = includeId
                ? loadExistingStockForUpdate(product.getProductId())
                : new LinkedHashMap<>();

        product.setVariants(readVariantsFromRequest(request, existingStockByVariantId));

        // Upload new image if staff chose a file
        Part imagePart = getImagePartSafely(request);
        if (imagePart != null && imagePart.getSize() > 0) {
            String uploadedImageUrl = saveImageFile(request, imagePart);
            if (uploadedImageUrl != null) {
                product.setPrimaryImageUrl(uploadedImageUrl);
            }
        }

        return product;
    }

    /**
     * On edit: load old product to keep stockQty for existing variants.
     * NEW variants (no variantId) get stockQty = 0 (warehouse will update later).
     */
    private Map<String, int[]> loadExistingStockForUpdate(String productId) {
        Map<String, int[]> map = new LinkedHashMap<>();
        if (productId == null || productId.isBlank()) return map;
        Product existing = productService.getProduct(productId);
        if (existing == null) return map;
        for (ProductVariant v : existing.getVariants()) {
            if (v.getVariantId() != null) {
                map.put(v.getVariantId(), new int[]{ v.getStockQty() });
            }
        }
        return map;
    }

    private Category buildCategoryFromRequest(HttpServletRequest request) {
        Category category = new Category();
        category.setCategoryId(getTrimmedParam(request, "categoryId"));
        category.setName(getTrimmedParam(request, "name"));
        return category;
    }

    private Color buildColorFromRequest(HttpServletRequest request) {
        Color color = new Color();
        color.setColorId(getTrimmedParam(request, "colorId"));
        color.setColorName(getTrimmedParam(request, "colorName"));
        color.setHexCode(normalizeHex(getTrimmedParam(request, "hexCode")));
        return color;
    }

    private Size buildSizeFromRequest(HttpServletRequest request) {
        Size size = new Size();
        size.setSizeId(getTrimmedParam(request, "sizeId"));
        size.setSizeName(getTrimmedParam(request, "sizeName"));
        return size;
    }

    private String validateProduct(Product product, boolean isUpdate) {
        if (product == null) return "Product is missing.";
        if (!isUpdate && isBlank(product.getPrimaryImageUrl())) return "Please choose a product image.";
        if (isBlank(product.getCategoryId())) return "Please choose a category.";
        if (isBlank(product.getName())) return "Please enter product name.";
        if (product.getName().length() > 200) return "Product name is too long (max 200 characters).";
        if (product.getBasePrice() == null) return "Please enter base price.";
        if (product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) return "Base price must be greater than 0.";
        if (!VALID_STATUSES.contains(product.getStatus())) return "Invalid status.";

        // Variant: empty allowed (staff may not need any variant)
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant == null) continue;
                if (isBlank(variant.getSizeId())) return "Each variant must have a size.";
                if (isBlank(variant.getColorId())) return "Each variant must have a color.";
                if (variant.getStockQty() < 0) return "Stock quantity cannot be negative.";
            }
        }
        return null;
    }

    private String validateCategory(Category category) {
        if (category == null) return "Category is missing.";
        if (isBlank(category.getName())) return "Please enter category name.";
        if (category.getName().length() > 200) return "Category name is too long (max 200 characters).";
        return null;
    }

    private String validateColor(Color color) {
        if (color == null) return "Color is missing.";
        if (isBlank(color.getColorName())) return "Please enter color name.";
        if (color.getColorName().length() > 100) return "Color name is too long (max 100 characters).";
        if (color.getHexCode() == null || !color.getHexCode().matches("^#[0-9A-Fa-f]{6}$")) {
            return "Please select a valid color.";
        }
        return null;
    }

    private String validateSize(Size size) {
        if (size == null) return "Size is missing.";
        if (isBlank(size.getSizeName())) return "Please enter size name.";
        if (size.getSizeName().length() > 50) return "Size name is too long (max 50 characters).";
        return null;
    }

    // ============ Load reference data ============

    private void loadReferenceData(HttpServletRequest request, String categoryId) {
        request.setAttribute("categories", categoryDAO.getAllCategories());
        request.setAttribute("statuses", VALID_STATUSES);
        request.setAttribute("sizes", sizeDAO.getAllSizes());
        request.setAttribute("allSizes", sizeDAO.getAllSizes());
        request.setAttribute("colors", colorDAO.getAllColors());
    }

    /**
     * Return full product JSON (with variants, colorNames, sizeNames, totalStockQty)
     * so the edit modal on listProduct.jsp can fill the form without leaving the page.
     */
    private void writeProductJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String productId = getTrimmedParam(request, "id");
        if (productId.isBlank()) {
            response.getWriter().write("{\"error\":\"Missing product id\"}");
            return;
        }

        Product product = productService.getProduct(productId);
        if (product == null) {
            response.getWriter().write("{\"error\":\"Product not found\"}");
            return;
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"productId\":\"").append(escapeJson(product.getProductId())).append("\",");
        json.append("\"categoryId\":\"").append(escapeJson(product.getCategoryId())).append("\",");
        json.append("\"categoryName\":\"").append(escapeJson(product.getCategoryName())).append("\",");
        json.append("\"name\":\"").append(escapeJson(product.getName())).append("\",");
        json.append("\"description\":\"").append(escapeJson(product.getDescription())).append("\",");
        json.append("\"basePrice\":").append(product.getBasePrice() != null ? product.getBasePrice().toPlainString() : "0").append(",");
        json.append("\"status\":\"").append(escapeJson(product.getStatus())).append("\",");
        json.append("\"primaryImageUrl\":\"").append(escapeJson(product.getPrimaryImageUrl())).append("\",");
        json.append("\"totalStockQty\":").append(product.getTotalStockQty()).append(",");

        json.append("\"variants\":[");
        List<ProductVariant> variants = product.getVariants();
        for (int i = 0; i < variants.size(); i++) {
            if (i > 0) json.append(",");
            ProductVariant v = variants.get(i);
            json.append("{")
                    .append("\"variantId\":\"").append(escapeJson(v.getVariantId())).append("\",")
                    .append("\"sizeId\":\"").append(escapeJson(v.getSizeId())).append("\",")
                    .append("\"sizeName\":\"").append(escapeJson(v.getSizeName())).append("\",")
                    .append("\"colorId\":\"").append(escapeJson(v.getColorId())).append("\",")
                    .append("\"colorName\":\"").append(escapeJson(v.getColorName())).append("\",")
                    .append("\"sku\":\"").append(escapeJson(v.getSku())).append("\",")
                    .append("\"stockQty\":").append(v.getStockQty()).append(",")
                    .append("\"availableQty\":").append(v.getAvailableQty()).append(",")
                    .append("\"priceOverride\":")
                    .append(v.getPriceOverride() != null ? v.getPriceOverride().toPlainString() : "null")
                    .append("}");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    // ============ Read variants from form ============

    private List<ProductVariant> readVariantsFromRequest(HttpServletRequest request) {
        return readVariantsFromRequest(request, new LinkedHashMap<>());
    }

    private List<ProductVariant> readVariantsFromRequest(HttpServletRequest request,
                                                          Map<String, int[]> existingStockByVariantId) {
        String[] variantIds = request.getParameterValues("variantId");
        String[] sizeIds = request.getParameterValues("variantSizeId");
        String[] colorIds = request.getParameterValues("variantColorId");
        String[] skus = request.getParameterValues("variantSku");
        String[] stockQtys = request.getParameterValues("variantStockQty");
        String[] priceOverrides = request.getParameterValues("variantPriceOverride");
        String[] enableds = request.getParameterValues("variantEnabled");

        int rowCount = maxLength(variantIds, sizeIds, colorIds, skus, stockQtys, priceOverrides, enableds);
        List<ProductVariant> variants = new ArrayList<>();
        Map<String, ProductVariant> dedup = new LinkedHashMap<>();

        for (int i = 0; i < rowCount; i++) {
            String enabled = getArrayValue(enableds, i);
            String variantId = getArrayValue(variantIds, i);
            String sizeId = getArrayValue(sizeIds, i);
            String colorId = getArrayValue(colorIds, i);
            String sku = getArrayValue(skus, i);
            String stockQtyRaw = getArrayValue(stockQtys, i);
            String priceOverrideRaw = normalizeCurrencyValue(getArrayValue(priceOverrides, i));

            if ("false".equalsIgnoreCase(enabled)) continue;
            if (isAllBlank(variantId, sizeId, colorId, sku, stockQtyRaw, priceOverrideRaw)) continue;

            ProductVariant variant = new ProductVariant();
            if (variantId != null && !variantId.isBlank()) {
                variant.setVariantId(variantId);
            }
            variant.setSizeId(sizeId);
            variant.setColorId(colorId);
            variant.setSku(sku);

// IMPORTANT: existing variant (has variantId) keeps stockQty from DB.
//            new variant (no variantId) defaults stock = 0 (warehouse updates later).
            if (variant.getVariantId() != null && existingStockByVariantId.containsKey(variant.getVariantId())) {
                int[] stockInfo = existingStockByVariantId.get(variant.getVariantId());
                variant.setStockQty(stockInfo[0]);
            } else {
                variant.setStockQty(parseInteger(stockQtyRaw));
            }
            variant.setPriceOverride(parseBigDecimal(priceOverrideRaw));
            dedup.put(safe(sizeId) + "::" + safe(colorId), variant);
        }

        variants.addAll(dedup.values());
        return variants;
    }

    // ============ JSON endpoints for modal Category / Color / Size ============

    private void writeCategoryJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String id = getTrimmedParam(request, "id");
        Category category = id.isBlank() ? null : categoryDAO.getCategoryById(id);
        if (category == null) {
            response.getWriter().write("{\"error\":\"Category not found\"}");
            return;
        }
        response.getWriter().write("{"
                + "\"categoryId\":\"" + escapeJson(category.getCategoryId()) + "\","
                + "\"name\":\"" + escapeJson(category.getName()) + "\""
                + "}");
    }

    private void writeColorJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String id = getTrimmedParam(request, "id");
        Color color = id.isBlank() ? null : colorDAO.getColorById(id);
        if (color == null) {
            response.getWriter().write("{\"error\":\"Color not found\"}");
            return;
        }
        response.getWriter().write("{"
                + "\"colorId\":\"" + escapeJson(color.getColorId()) + "\","
                + "\"colorName\":\"" + escapeJson(color.getColorName()) + "\","
                + "\"hexCode\":\"" + escapeJson(color.getHexCode()) + "\""
                + "}");
    }

    private void writeSizeJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String id = getTrimmedParam(request, "id");
        Size size = id.isBlank() ? null : sizeDAO.getSizeById(id);
        if (size == null) {
            response.getWriter().write("{\"error\":\"Size not found\"}");
            return;
        }
        response.getWriter().write("{"
                + "\"sizeId\":\"" + escapeJson(size.getSizeId()) + "\","
            + "\"sizeName\":\"" + escapeJson(size.getSizeName()) + "\""
                + "}");
    }

    // ============ Image upload ============

    private Part getImagePartSafely(HttpServletRequest request) {
        try {
            return request.getPart("productImage");
        } catch (Exception ex) {
            return null;
        }
    }

    private Part getVariantImagePartSafely(HttpServletRequest request) {
        try {
            return request.getPart("variantImage");
        } catch (Exception ex) {
            return null;
        }
    }

    private String saveImageFile(HttpServletRequest request, Part imagePart) throws IOException {
        String rawName = imagePart.getSubmittedFileName();
        if (rawName == null || rawName.isBlank()) return null;

        String submittedFileName = Paths.get(rawName).getFileName().toString();
        if (submittedFileName.isBlank()) return null;

        String extension = "";
        int dot = submittedFileName.lastIndexOf('.');
        if (dot >= 0) extension = submittedFileName.substring(dot);

        String storedFileName = "product-" + UUID.randomUUID().toString().replace("-", "") + extension;

        Path uploadDir = getExternalUploadDirectory();
        Files.createDirectories(uploadDir);
        Path destination = uploadDir.resolve(storedFileName).normalize();
        imagePart.write(destination.toAbsolutePath().toString());

        return "/assets/product-images/" + storedFileName;
    }

    public static Path getExternalUploadDirectory() {
        String externalPath = System.getProperty("fashion.upload.path");
        if (externalPath != null && !externalPath.isBlank()) {
            return Paths.get(externalPath, "product-images");
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            return Paths.get(userHome, ".fashion-system", "product-images");
        }

        return Paths.get(System.getProperty("java.io.tmpdir"), "fashion-management-system", "product-images");
    }

    private boolean isProductImageRequest(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        return "/assets/product-images".equals(servletPath)
                || (servletPath != null && servletPath.startsWith("/assets/product-images"))
                || ("/staff/products".equals(servletPath) && pathInfo != null && pathInfo.startsWith("/assets/product-images/"));
    }

    private void serveProductImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String imageName = extractRequestedImageName(request);
        if (imageName == null || imageName.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path imagePath = getExternalUploadDirectory().resolve(imageName).normalize();
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
        if (path == null || path.isBlank()) path = request.getServletPath();
        if (path == null || path.isBlank()) return null;

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == path.length() - 1) return null;
        return Paths.get(path.substring(lastSlash + 1)).getFileName().toString();
    }

    // ============ Utils ============

    private boolean checkStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object userObject = request.getSession(false) != null
                ? request.getSession(false).getAttribute("USER") : null;

        if (!(userObject instanceof Account)) {
            // If AJAX request, return 401 JSON instead of redirect HTML
            if (isAjax(request)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Session expired. Please log in again.\"}");
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        Account account = (Account) userObject;
        String role = account.getRole();
        if (role == null || !role.equalsIgnoreCase("Staff")) {
            if (isAjax(request)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"You don't have permission to perform this action.\"}");
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }
        return true;
    }

    private boolean isAjax(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return header != null && header.equalsIgnoreCase("XMLHttpRequest");
    }

    private String getAction(HttpServletRequest request) {
        String action = request.getParameter("action");
        return action != null ? action : "list";
    }

    private String getTrimmedParam(HttpServletRequest request, String param) {
        return getTrimmedParam(request, param, null);
    }

    private String getTrimmedParam(HttpServletRequest request, String param, String defaultValue) {
        String value = request.getParameter(param);
        return value != null ? value.trim() : defaultValue;
    }

    private int maxLength(String[]... arrays) {
        int max = 0;
        for (String[] array : arrays) {
            if (array != null && array.length > max) max = array.length;
        }
        return max;
    }

    private String getArrayValue(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) return null;
        return trimToNull(values[index]);
    }

    private boolean isAllBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        return value != null ? value.trim() : null;
    }

    private int parseInteger(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return 0;
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private BigDecimal parseBigDecimal(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return null;
        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeCurrencyValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return null;
        return rawValue.replace("đ", "").replace("₫", "")
                .replace("VND", "").replace("vnd", "")
                .replace(".", "").replace(",", "")
                .replaceAll("\\s+", "").trim();
    }

    private String normalizeHex(String rawHex) {
        if (rawHex == null || rawHex.isBlank()) return null;
        String hex = rawHex.trim().toUpperCase();
        if (!hex.startsWith("#")) hex = "#" + hex;
        return hex;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Return unified JSON response for all modal forms (create/edit/delete).
     * Client reads {success: bool, message: "..."}.
     */
    private void writeJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{"
                + "\"success\":" + success + ","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}");
    }

    // ============ Check restrictions before delete ============

    private String checkCategoryInUse(String categoryId) {
        int count = productDAO.countByCategoryId(categoryId);
        if (count > 0) {
            return "Cannot delete: category is currently used by " + count + " product(s).";
        }
        return null;
    }

    private String checkColorInUse(String colorId) {
        int count = variantDAO.countByColorId(colorId);
        if (count > 0) {
            return "Cannot delete: color is currently used by " + count + " variant(s).";
        }
        return null;
    }

    private String checkSizeInUse(String sizeId) {
        int count = variantDAO.countBySizeId(sizeId);
        if (count > 0) {
            return "Cannot delete: size is currently used by " + count + " variant(s).";
        }
        return null;
    }

    
    private String translateDbError(java.sql.SQLException ex, String entityLabel) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("unique") || msg.contains("duplicate") || msg.contains("uq_") || msg.contains("pk_")) {
            return entityLabel.substring(0, 1).toUpperCase() + entityLabel.substring(1)
                    + " name already exists. Please choose a different name.";
        }
        if (msg.contains("foreign key") || msg.contains("reference")) {
            return "Cannot delete: this " + entityLabel + " is referenced by other records.";
        }
        // Fallback - log full error for dev
        System.out.println("[" + entityLabel + "] DB error: " + ex.getMessage());
        return "Database error while saving " + entityLabel + ". Please try again.";
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String buildQuery(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .filter(entry -> !"page".equals(entry.getKey()))
                .flatMap(entry -> Arrays.stream(entry.getValue()).map(value -> entry.getKey() + "=" + value))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private <T> PageSlice<T> paginate(List<T> items, int requestedPage, int pageSize) {
        int totalItems = items.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.min(Math.max(1, requestedPage), totalPages);
        int fromIndex = Math.max(0, (currentPage - 1) * pageSize);
        int toIndex = Math.min(totalItems, fromIndex + pageSize);
        List<T> pageItems = fromIndex < toIndex ? items.subList(fromIndex, toIndex) : List.of();
        return new PageSlice<>(pageItems, currentPage, totalPages, totalItems);
    }

    private record PageSlice<T>(List<T> items, int currentPage, int totalPages, int totalItems) {
        public List<T> items() { return items; }
        public int currentPage() { return currentPage; }
        public int totalPages() { return totalPages; }
    }

    // ============ Redirect ============

    private void redirectToList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    private void redirectWithMessage(HttpServletRequest request, HttpServletResponse response,
                                     String message, String type) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/products?message=" + url(message) + "&messageType=" + type);
    }

    private void redirectWithTab(HttpServletRequest request, HttpServletResponse response,
                                String tab, String message, String type) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/products?tab=" + tab
                + "&message=" + url(message) + "&messageType=" + type);
    }

    private String url(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception ex) {
            return value;
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  Product product, String formAction, String pageTitle, String error)
            throws ServletException, IOException {
        loadReferenceData(request, product.getCategoryId());
        request.setAttribute("product", product);
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("error", error);
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }
}