package Services;

import java.math.BigDecimal;
import java.util.List;

import DALs.OrderDAO;
import DALs.ProductDAO;
import DALs.ProductImageDAO;
import DALs.ProductVariantDAO;
import Models.Product;
import Models.ProductVariant;

/**
 * ProductService - Business logic for Product.
 *
 * Simple code for students.
 * Staff can edit all product fields (no restrictions).
 */
public class ProductService {

    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private ProductImageDAO imageDAO;
    private OrderDAO orderDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.variantDAO = new ProductVariantDAO();
        this.imageDAO = new ProductImageDAO();
        this.orderDAO = new OrderDAO();
    }

    // ============ Read ============

    public ProductDAO.ProductResult getProducts(String keyword, String status, String categoryId,
                                                int page, int pageSize) {
        return productDAO.getProductsFiltered(keyword, status, categoryId, page, pageSize);
    }

    public Product getProduct(String productId) {
        if (isBlank(productId)) return null;
        return productDAO.getProductById(productId);
    }

    public List<Product> getLatestProducts(int limit) {
        return productDAO.getLatestProducts(limit);
    }

    // ============ Create ============

    public boolean createProduct(Product product) {
        if (product == null) return false;
        if (!productDAO.createProduct(product)) return false;

        // Save primary image (if any)
        if (!isBlank(product.getPrimaryImageUrl())) {
            imageDAO.upsertPrimaryImage(product.getProductId(), product.getPrimaryImageUrl());
        }

        // Save variants (if any)
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variantDAO.replaceVariants(product.getProductId(), product.getVariants());
        }

        return true;
    }

    // ============ Update - staff can edit anything ============

    public boolean updateProduct(Product product) {
        if (product == null || isBlank(product.getProductId())) return false;

        // Get old image url to delete later if changed
        Product oldProduct = productDAO.getProductById(product.getProductId());
        String oldImageUrl = oldProduct != null ? oldProduct.getPrimaryImageUrl() : null;

        if (!productDAO.updateProduct(product)) return false;

        // Update primary image if a new one was uploaded
        if (!isBlank(product.getPrimaryImageUrl())) {
            imageDAO.upsertPrimaryImage(product.getProductId(), product.getPrimaryImageUrl());

            // Delete old image file if it changed
            if (oldImageUrl != null && !oldImageUrl.equals(product.getPrimaryImageUrl())) {
                imageDAO.deleteImageFile(oldImageUrl);
            }
        }

        // Update variants
        if (product.getVariants() != null) {
            variantDAO.replaceVariants(product.getProductId(), product.getVariants());
        }

        return true;
    }

    // ============ Delete - staff can delete any product ============

    /**
     * Delete product.
     *
     * Pre-checks Pending orders referencing the product so we can return a
     * clear message instead of letting SQL throw a generic FK violation.
     * Orders in any other state (Confirmed, Processing, Shipping, Delivered,
     * Cancelled) do NOT block deletion - they are kept as history records
     * (FK in DB still allows deleting variants because the OrderItems rows
     * themselves are removed via cascade in the same transaction).
     * Image files are only deleted once the DB transaction has actually
     * committed.
     */
    public DeleteResult deleteProduct(String productId) {
        if (isBlank(productId)) return new DeleteResult(false, "Invalid product id.");

        Product product = productDAO.getProductById(productId);
        if (product == null) return new DeleteResult(false, "Product does not exist or was already deleted.");

        int pendingCount = orderDAO.countPendingOrdersByProductId(productId);
        if (pendingCount > 0) {
            return new DeleteResult(false,
                    "Cannot delete: this product has " + pendingCount
                            + " order(s) still pending. Wait until they are confirmed or cancel them first.");
        }

        try {
            boolean ok = productDAO.deleteProduct(productId);
            if (!ok) {
                return new DeleteResult(false, "Cannot delete product. Please try again.");
            }

            // Only touch the file system AFTER the DB commit succeeded
            String oldImageUrl = product.getPrimaryImageUrl();
            if (!isBlank(oldImageUrl)) {
                imageDAO.deleteImageFile(oldImageUrl);
            }
            return new DeleteResult(true, "Product deleted successfully.");
        } catch (RuntimeException ex) {
            String root = ex.getMessage() != null ? ex.getMessage() : "";
            String msg;
            if (root.toLowerCase().contains("warehouseimports")) {
                msg = "Cannot delete: this product is still referenced by warehouse import records.";
            } else if (root.toLowerCase().contains("reference")) {
                msg = "Cannot delete: this product is referenced by other records (orders, comments, etc.).";
            } else if (!root.isBlank()) {
                msg = "Cannot delete product: " + root;
            } else {
                msg = "Cannot delete product. Please try again.";
            }
            return new DeleteResult(false, msg);
        }
    }

    /** Simple boolean + message holder for deleteProduct. */
    public static class DeleteResult {
        public final boolean success;
        public final String message;

        public DeleteResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    // ============ Validate ============

    /**
     * Check product validity. Return null if valid, else return error message.
     */
    public String validateProduct(Product product) {
        if (product == null) return "Product is missing.";
        if (isBlank(product.getCategoryId())) return "Please choose a category.";
        if (isBlank(product.getName())) return "Please enter product name.";
        if (product.getName().length() > 200) return "Product name is too long (max 200 characters).";
        if (product.getBasePrice() == null) return "Please enter base price.";
        if (product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) return "Base price must be greater than 0.";
        if (isBlank(product.getStatus())) return "Please choose status.";
        return null;
    }

    public String validateVariant(ProductVariant variant) {
        if (variant == null) return "Variant is missing.";
        if (isBlank(variant.getSizeId())) return "Please choose a size.";
        if (isBlank(variant.getColorId())) return "Please choose a color.";
        if (variant.getStockQty() < 0) return "Stock cannot be negative.";
        return null;
    }

    // ============ Utils ============

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public boolean isDatabaseReady() {
        return productDAO.isDatabaseReady();
    }
}