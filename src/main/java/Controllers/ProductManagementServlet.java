package Controllers;

import DALs.CategoryDAO;
import DALs.ColorDAO;
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

@WebServlet(name = "ProductManagementServlet", urlPatterns = {"/staff/products", "/assets/product-images/*"})
@MultipartConfig
public class ProductManagementServlet extends HttpServlet {

    private static final List<String> VALID_STATUSES = Arrays.asList("Available", "OutOfStock", "Inactive");
    private static final int DEFAULT_PAGE_SIZE = 8;

    private ProductService productService;
    private CategoryDAO categoryDAO;
    private ColorDAO colorDAO;
    private SizeDAO sizeDAO;

    @Override
    public void init() {
        productService = new ProductService();
        categoryDAO = new CategoryDAO();
        colorDAO = new ColorDAO();
        sizeDAO = new SizeDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (isProductImageRequest(request)) {
            serveProductImage(request, response);
            return;
        }

        if (!isStaff(request, response)) {
            return;
        }

        String action = getAction(request);

        switch (action) {
            case "create" -> showCreateForm(request, response);
            case "edit" -> showEditForm(request, response);
            case "delete" -> showDeleteForm(request, response);
            case "sizesByCategory" -> writeSizesByCategoryJson(request, response);
            case "createCategory" -> showCategoryForm(request, response, new Category(), "createCategory", "Add Category");
            case "editCategory" -> showEditCategoryForm(request, response);
            case "deleteCategory" -> showDeleteCategoryForm(request, response);
            case "createColor" -> showColorForm(request, response, new Color(), "createColor", "Add Color");
            case "editColor" -> showEditColorForm(request, response);
            case "deleteColor" -> showDeleteColorForm(request, response);
            case "createSize" -> showSizeForm(request, response, new Size(), "createSize", "Add Size");
            case "editSize" -> showEditSizeForm(request, response);
            case "deleteSize" -> showDeleteSizeForm(request, response);
            default -> showProductList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isStaff(request, response)) {
            return;
        }

        String action = getAction(request);

        switch (action) {
            case "create" -> createProduct(request, response);
            case "edit" -> updateProduct(request, response);
            case "delete" -> deleteProduct(request, response);
            case "createCategory" -> createCategory(request, response);
            case "editCategory" -> updateCategory(request, response);
            case "deleteCategory" -> deleteCategory(request, response);
            case "createColor" -> createColor(request, response);
            case "editColor" -> updateColor(request, response);
            case "deleteColor" -> deleteColor(request, response);
            case "createSize" -> createSize(request, response);
            case "editSize" -> updateSize(request, response);
            case "deleteSize" -> deleteSize(request, response);
            default -> redirectToList(request, response);
        }
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

        List<Category> allCategories = categoryDAO.getAllCategories();
        List<Color> allColors = colorDAO.getAllColors();
        List<Size> allSizes = sizeDAO.getAllSizes();

        // Group sizes by category
        Map<String, List<Size>> sizesByCategory = new LinkedHashMap<>();
        for (Category cat : allCategories) {
            sizesByCategory.put(cat.getCategoryId(), new ArrayList<>());
        }
        for (Size size : allSizes) {
            if (size.getCategoryId() != null && sizesByCategory.containsKey(size.getCategoryId())) {
                sizesByCategory.get(size.getCategoryId()).add(size);
            }
        }

        PageSlice<Category> categoryPage = paginate(allCategories, currentPage, DEFAULT_PAGE_SIZE);
        PageSlice<Color> colorPage = paginate(allColors, currentPage, DEFAULT_PAGE_SIZE);
        PageSlice<Size> sizePage = paginate(allSizes, currentPage, DEFAULT_PAGE_SIZE);

        PageSlice<?> activePage = switch (activeTab) {
            case "categories" -> categoryPage;
            case "colors" -> colorPage;
            case "sizes" -> sizePage;
            default -> new PageSlice<>(productResult.products(), currentPage,
                    productResult.totalPages(DEFAULT_PAGE_SIZE), productResult.totalCount());
        };

        request.setAttribute("products", productResult.products());
        request.setAttribute("totalProducts", productResult.totalCount());
        request.setAttribute("categoryItems", categoryPage.items());
        request.setAttribute("totalCategories", allCategories.size());
        request.setAttribute("allCategoryItems", allCategories);
        request.setAttribute("colorItems", colorPage.items());
        request.setAttribute("totalColors", allColors.size());
        request.setAttribute("sizeItems", sizePage.items());
        request.setAttribute("totalSizes", allSizes.size());
        request.setAttribute("sizesByCategory", sizesByCategory);
        request.setAttribute("currentPage", activePage.currentPage());
        request.setAttribute("totalPages", activePage.totalPages());
        request.setAttribute("productQuery", buildProductManagementQuery(request));

        request.getRequestDispatcher("/views/pages/productManagement/listProduct.jsp").forward(request, response);
    }

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
            redirectWithMessage(request, response, "Product-not-found", "error");
            return;
        }

        request.setAttribute("product", product);
        request.setAttribute("formAction", "edit");
        request.setAttribute("pageTitle", "Update Product");
        loadReferenceData(request, product.getCategoryId());
        request.getRequestDispatcher("/views/pages/productManagement/productForm.jsp").forward(request, response);
    }

    private void showDeleteForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productId = request.getParameter("id");
        Product product = productService.getProduct(productId);

        if (product == null) {
            redirectWithMessage(request, response, "Product-not-found", "error");
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/views/pages/productManagement/deleteProduct.jsp").forward(request, response);
    }

    private void createProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Product product = buildProductFromRequest(request, false);
        String error = validateProduct(product);

        if (error != null) {
            forwardWithError(request, response, product, "create", "Add Product", error);
            return;
        }

        boolean created = productService.createProduct(product);
        if (created) {
            redirectWithMessage(request, response, "Product-created-successfully", "success");
        } else {
            forwardWithError(request, response, product, "create", "Add Product", "Unable-to-create-product");
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Product product = buildProductFromRequest(request, true);
        String error = validateProduct(product);

        if (error != null) {
            forwardWithError(request, response, product, "edit", "Update Product", error);
            return;
        }

        boolean updated = productService.updateProduct(product);
        if (updated) {
            redirectWithMessage(request, response, "Product-updated-successfully", "success");
        } else {
            forwardWithError(request, response, product, "edit", "Update Product", "Unable-to-update-product");
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String productId = request.getParameter("productId");

        if (productId == null || productId.isBlank()) {
            redirectWithMessage(request, response, "Invalid-product-id", "error");
            return;
        }

        boolean deleted = productService.deleteProduct(productId);
        String message = deleted ? "Product-deleted-successfully" : "Unable-to-delete-product-in-use";
        String type = deleted ? "success" : "error";
        redirectWithMessage(request, response, message, type);
    }

    private void showEditCategoryForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryDAO.getCategoryById(request.getParameter("id"));
        if (category == null) {
            redirectWithTab(request, response, "categories", "Category-not-found", "error");
            return;
        }
        showCategoryForm(request, response, category, "editCategory", "Update Category");
    }

    private void showDeleteCategoryForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryDAO.getCategoryById(request.getParameter("id"));
        if (category == null) {
            redirectWithTab(request, response, "categories", "Category-not-found", "error");
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
            request.setAttribute("error", error);
            showCategoryForm(request, response, category, "createCategory", "Add Category");
            return;
        }

        boolean created = categoryDAO.createCategory(category);
        if (created) {
            redirectWithTab(request, response, "categories", "Category-created-successfully", "success");
        } else {
            request.setAttribute("error", "Unable-to-create-category");
            showCategoryForm(request, response, category, "createCategory", "Add Category");
        }
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = buildCategoryFromRequest(request);
        String error = validateCategory(category);

        if (error != null) {
            request.setAttribute("error", error);
            showCategoryForm(request, response, category, "editCategory", "Update Category");
            return;
        }

        boolean updated = categoryDAO.updateCategory(category);
        if (updated) {
            redirectWithTab(request, response, "categories", "Category-updated-successfully", "success");
        } else {
            request.setAttribute("error", "Unable-to-update-category");
            showCategoryForm(request, response, category, "editCategory", "Update Category");
        }
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String categoryId = request.getParameter("categoryId");
        if (categoryId == null || categoryId.isBlank()) {
            redirectWithTab(request, response, "categories", "Invalid-category-id", "error");
            return;
        }

        boolean deleted = categoryDAO.deleteCategory(categoryId);
        String message = deleted ? "Category-deleted-successfully" : "Unable-to-delete-category-in-use";
        String type = deleted ? "success" : "error";
        redirectWithTab(request, response, "categories", message, type);
    }

    private void showEditColorForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = colorDAO.getColorById(request.getParameter("id"));
        if (color == null) {
            redirectWithTab(request, response, "colors", "Color-not-found", "error");
            return;
        }
        showColorForm(request, response, color, "editColor", "Update Color");
    }

    private void showDeleteColorForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = colorDAO.getColorById(request.getParameter("id"));
        if (color == null) {
            redirectWithTab(request, response, "colors", "Color-not-found", "error");
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
            request.setAttribute("error", error);
            showColorForm(request, response, color, "createColor", "Add Color");
            return;
        }

        boolean created = colorDAO.createColor(color);
        if (created) {
            redirectWithTab(request, response, "colors", "Color-created-successfully", "success");
        } else {
            request.setAttribute("error", "Unable-to-create-color");
            showColorForm(request, response, color, "createColor", "Add Color");
        }
    }

    private void updateColor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Color color = buildColorFromRequest(request);
        String error = validateColor(color);

        if (error != null) {
            request.setAttribute("error", error);
            showColorForm(request, response, color, "editColor", "Update Color");
            return;
        }

        boolean updated = colorDAO.updateColor(color);
        if (updated) {
            redirectWithTab(request, response, "colors", "Color-updated-successfully", "success");
        } else {
            request.setAttribute("error", "Unable-to-update-color");
            showColorForm(request, response, color, "editColor", "Update Color");
        }
    }

    private void deleteColor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String colorId = request.getParameter("colorId");
        if (colorId == null || colorId.isBlank()) {
            redirectWithTab(request, response, "colors", "Invalid-color-id", "error");
            return;
        }

        boolean deleted = colorDAO.deleteColor(colorId);
        String message = deleted ? "Color-deleted-successfully" : "Unable-to-delete-color-in-use";
        String type = deleted ? "success" : "error";
        redirectWithTab(request, response, "colors", message, type);
    }

    private void showEditSizeForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = sizeDAO.getSizeById(request.getParameter("id"));
        if (size == null) {
            redirectWithTab(request, response, "sizes", "Size-not-found", "error");
            return;
        }
        showSizeForm(request, response, size, "editSize", "Update Size");
    }

    private void showDeleteSizeForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = sizeDAO.getSizeById(request.getParameter("id"));
        if (size == null) {
            redirectWithTab(request, response, "sizes", "Size-not-found", "error");
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
        request.setAttribute("categories", categoryDAO.getAllCategories());
        request.getRequestDispatcher("/views/pages/productManagement/sizeForm.jsp").forward(request, response);
    }

    private void createSize(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = buildSizeFromRequest(request);
        String error = validateSize(size);

        if (error != null) {
            request.setAttribute("error", error);
            showSizeForm(request, response, size, "createSize", "Add Size");
            return;
        }

        boolean created = sizeDAO.createSize(size);
        if (created) {
            redirectWithTab(request, response, "sizes", "Size-created-successfully", "success");
        } else {
            request.setAttribute("error", "Unable-to-create-size");
            showSizeForm(request, response, size, "createSize", "Add Size");
        }
    }

    private void updateSize(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Size size = buildSizeFromRequest(request);
        String error = validateSize(size);

        if (error != null) {
            request.setAttribute("error", error);
            showSizeForm(request, response, size, "editSize", "Update Size");
            return;
        }

        boolean updated = sizeDAO.updateSize(size);
        if (updated) {
            redirectWithTab(request, response, "sizes", "Size-updated-successfully", "success");
        } else {
            request.setAttribute("error", "Unable-to-update-size");
            showSizeForm(request, response, size, "editSize", "Update Size");
        }
    }

    private void deleteSize(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sizeId = request.getParameter("sizeId");
        if (sizeId == null || sizeId.isBlank()) {
            redirectWithTab(request, response, "sizes", "Invalid-size-id", "error");
            return;
        }

        boolean deleted = sizeDAO.deleteSize(sizeId);
        String message = deleted ? "Size-deleted-successfully" : "Unable-to-delete-size-in-use";
        String type = deleted ? "success" : "error";
        redirectWithTab(request, response, "sizes", message, type);
    }

    private Product buildProductFromRequest(HttpServletRequest request, boolean includeId)
            throws IOException, ServletException {
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

        product.setVariants(readVariantsFromRequest(request));
        hydrateVariantSummaries(product);

        Part imagePart = getImagePartSafely(request);
        if (imagePart != null && imagePart.getSize() > 0) {
            String uploadedImageUrl = saveImageFile(request, imagePart);
            if (uploadedImageUrl != null) {
                product.setPrimaryImageUrl(uploadedImageUrl);
            }
        }

        return product;
    }

    private Category buildCategoryFromRequest(HttpServletRequest request) {
        Category category = new Category();
        category.setCategoryId(getTrimmedParam(request, "categoryId"));
        category.setName(getTrimmedParam(request, "name"));
        category.setDescription(getTrimmedParam(request, "description"));
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
        size.setCategoryId(getTrimmedParam(request, "categoryId"));
        return size;
    }

    private String validateProduct(Product product) {
        if (isBlank(product.getCategoryId())) return "Please select a category.";
        if (isBlank(product.getName())) return "Please enter product name.";
        if (product.getName().length() > 200) return "Product name cannot exceed 200 characters.";
        if (product.getBasePrice() == null) return "Please enter a valid base price.";
        if (product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) return "Base price must be greater than 0.";
        if (!VALID_STATUSES.contains(product.getStatus())) return "Invalid status.";
        if (product.getVariants() == null || product.getVariants().isEmpty()) return "Please add at least one sales variant for the product.";
        for (ProductVariant variant : product.getVariants()) {
            if (isBlank(variant.getSizeId())) return "Each variant must have a size.";
            if (isBlank(variant.getColorId())) return "Each variant must have a color.";
            if (variant.getStockQty() < 0) return "Stock quantity cannot be negative.";
            if (variant.getPriceOverride() != null && variant.getPriceOverride().compareTo(BigDecimal.ZERO) < 0) return "Price override cannot be negative.";
        }
        return null;
    }

    private String validateCategory(Category category) {
        if (isBlank(category.getName())) return "Please enter category name.";
        if (category.getName().length() > 200) return "Category name cannot exceed 200 characters.";
        return null;
    }

    private String validateColor(Color color) {
        if (isBlank(color.getColorName())) return "Please enter color name.";
        if (color.getColorName().length() > 100) return "Color name cannot exceed 100 characters.";
        if (color.getHexCode() == null || !color.getHexCode().matches("^#[0-9A-Fa-f]{6}$")) return "Please select a valid color.";
        return null;
    }

    private String validateSize(Size size) {
        if (isBlank(size.getSizeName())) return "Please enter size name.";
        if (size.getSizeName().length() > 50) return "Size name cannot exceed 50 characters.";
        if (isBlank(size.getCategoryId())) return "Please select a category for this size.";
        return null;
    }

    private void loadReferenceData(HttpServletRequest request, String categoryId) {
        request.setAttribute("categories", categoryDAO.getAllCategories());
        request.setAttribute("statuses", VALID_STATUSES);
        request.setAttribute("sizes", sizeDAO.getSizesByCategoryId(categoryId));
        request.setAttribute("allSizes", sizeDAO.getAllSizes());
        request.setAttribute("colors", colorDAO.getAllColors());
    }

    private void writeSizesByCategoryJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String categoryId = getTrimmedParam(request, "categoryId");
        List<Size> sizes = sizeDAO.getSizesByCategoryId(categoryId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < sizes.size(); i++) {
            if (i > 0) json.append(",");
            Size size = sizes.get(i);
            json.append("{\"sizeId\":\"").append(escapeJson(size.getSizeId()))
                    .append("\",\"sizeName\":\"").append(escapeJson(size.getSizeName()))
                    .append("\"}");
        }
        json.append("]");
        response.getWriter().write(json.toString());
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

            if (selectedSizeIds != null && selectedColorIds != null
                    && selectedSizeIds.length > 0 && selectedColorIds.length > 0) {
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

    private Part getImagePartSafely(HttpServletRequest request) {
        try {
            return request.getPart("productImage");
        } catch (Exception ex) {
            return null;
        }
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

        // Luu vao Assets/Images/Product
        String realPath = request.getServletContext().getRealPath("/");
        Path uploadDir = Paths.get(realPath, "Assets", "Images", "Product");
        Files.createDirectories(uploadDir);
        Path destination = uploadDir.resolve(storedFileName);
        imagePart.write(destination.toAbsolutePath().toString());

        String imageUrl = "/Assets/Images/Product/" + storedFileName;
        System.out.println("[ProductManagementServlet] Image saved: " + destination.toAbsolutePath());
        System.out.println("[ProductManagementServlet] Image URL: " + imageUrl);
        System.out.println("[ProductManagementServlet] File exists: " + Files.exists(destination));
        return imageUrl;
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

    private Path resolveUploadDirectory() {
        return getExternalUploadDirectory();
    }

    private String requestSafeRealPath() {
        try {
            return getServletContext().getRealPath("/");
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isProductImageRequest(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        return "/assets/product-images".equals(servletPath)
                || (servletPath != null && servletPath.startsWith("/assets/product-images"))
                || ("/staff/products".equals(servletPath) && pathInfo != null && pathInfo.startsWith("/assets/product-images/"));
    }

    private void serveProductImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String imageName = extractRequestedImageName(request);
        System.out.println("[ProductManagementServlet] serveProductImage called, imageName: " + imageName);
        if (imageName == null || imageName.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path imagePath = getExternalUploadDirectory().resolve(imageName).normalize();
        System.out.println("[ProductManagementServlet] Image path: " + imagePath.toAbsolutePath());
        System.out.println("[ProductManagementServlet] File exists: " + Files.exists(imagePath));
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

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex < 0 || lastSlashIndex == path.length() - 1) return null;
        return Paths.get(path.substring(lastSlashIndex + 1)).getFileName().toString();
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

    private boolean isStaff(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Object userObject = request.getSession(false) != null
                ? request.getSession(false).getAttribute("USER") : null;

        if (!(userObject instanceof Account)) {
            System.out.println("[ProductManagementServlet] Access denied: USER session missing");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        Account account = (Account) userObject;
        String role = account.getRole();
        if (role == null || (!role.equalsIgnoreCase("Staff"))) {
            System.out.println("[ProductManagementServlet] Access denied: non-staff user");
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }

        return true;
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
        return getTrimmed(values[index]);
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

    private String getTrimmed(String value) {
        return value != null ? value.trim() : null;
    }

    private int parseInteger(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return 0;
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException ex) {
            return -1;
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
        return rawValue.replace("đ", "").replace("₫", "").replace("VND", "").replace("vnd", "")
                .replace(".", "").replace(",", "").replaceAll("\\s+", "").trim();
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

    private void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    private void redirectWithMessage(HttpServletRequest request, HttpServletResponse response,
                                    String message, String type) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/products?message=" + message + "&messageType=" + type);
    }

    private void redirectWithTab(HttpServletRequest request, HttpServletResponse response,
                                String tab, String message, String type) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/products?tab=" + tab
                + "&message=" + message + "&messageType=" + type);
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
