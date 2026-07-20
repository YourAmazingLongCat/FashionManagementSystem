package Services;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import Controllers.ProductManagementServlet;
import DALs.ProductDAO;
import DALs.ProductImageDAO;
import DALs.ProductVariantDAO;
import Models.Product;
import Models.ProductVariant;

public class ProductService {

    private final ProductDAO productDAO;
    private final ProductVariantDAO variantDAO;
    private final ProductImageDAO imageDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.variantDAO = new ProductVariantDAO();
        this.imageDAO = new ProductImageDAO();
    }

    public ProductDAO.ProductResult getProducts(String keyword, String status, String categoryId, int page, int pageSize) {
        return getProducts(keyword, status, categoryId, null, null, null, page, pageSize);
    }

    public ProductDAO.ProductResult getProducts(String keyword, String status, String categoryId,
                                              String skuFilter, String sizeFilter, String colorFilter,
                                              int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        return productDAO.getProductsFiltered(keyword, status, categoryId, skuFilter, sizeFilter, colorFilter, page, pageSize);
    }

    public Product getProduct(String productId) {
        if (productId == null || productId.isBlank()) return null;
        return productDAO.getProductById(productId);
    }

    public List<Product> getLatestProducts(int limit) {
        if (limit < 1) limit = 10;
        if (limit > 50) limit = 50;
        return productDAO.getLatestProducts(limit);
    }

    public boolean createProduct(Product product) {
        if (!isValidForCreate(product)) return false;
        boolean productCreated = productDAO.createProduct(product);
        if (!productCreated) return false;
        return saveProductExtras(product, false);
    }

    public boolean updateProduct(Product product) {
        if (!isValidForUpdate(product)) return false;
        Product existing = productDAO.getProductById(product.getProductId());
        if (existing == null) return false;

        String oldImageUrl = existing.getPrimaryImageUrl();
        boolean productUpdated = productDAO.updateProduct(product);
        if (!productUpdated) return false;

        boolean result = saveProductExtras(product, true);

        if (result && product.getPrimaryImageUrl() != null
                && !product.getPrimaryImageUrl().isBlank()
                && (oldImageUrl == null || !oldImageUrl.equals(product.getPrimaryImageUrl()))) {
            deleteOldImageFile(oldImageUrl);
        }

        return result;
    }

    public boolean deleteProduct(String productId) {
        if (productId == null || productId.isBlank()) return false;
        Product existing = productDAO.getProductById(productId);
        boolean result = productDAO.deleteProduct(productId);
        if (result && existing != null && existing.getPrimaryImageUrl() != null) {
            deleteOldImageFile(existing.getPrimaryImageUrl());
        }
        return result;
    }

    private boolean saveProductExtras(Product product, boolean isUpdate) {
        if (product == null || product.getProductId() == null) return false;

        boolean imageSaved = true;
        if (product.getPrimaryImageUrl() != null && !product.getPrimaryImageUrl().isBlank()) {
            imageSaved = imageDAO.upsertPrimaryImage(product.getProductId(), product.getPrimaryImageUrl());
        }

        boolean variantsSaved = true;
        try {
            variantsSaved = variantDAO.replaceVariants(product.getProductId(), product.getVariants());
        } catch (Exception ex) {
            System.out.println("saveProductExtras: replaceVariants threw: " + ex.getMessage());
            variantsSaved = false;
        }

        // If variants replacement fails (for example due to FK constraints with existing order items),
        // do NOT delete the product. Permit updating description and primary image so staff can adjust
        // non-variant fields after checkout. Return success when at least the image update succeeded
        // or variant replacement succeeded.
        if (!variantsSaved) {
            System.out.println("saveProductExtras: variant replacement failed for product " + product.getProductId() + ", preserving existing variants and keeping product.");
        }

        return imageSaved || variantsSaved;
    }

    private void deleteOldImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String fileName = imageDAO.getImageFileNameByUrl(imageUrl);
            if (fileName == null || fileName.isBlank()) return;

            Path uploadDir = ProductManagementServlet.getExternalUploadDirectory();
            Path imagePath = uploadDir.resolve(fileName).normalize();

            if (Files.exists(imagePath) && Files.isRegularFile(imagePath)) {
                Files.delete(imagePath);
                System.out.println("Deleted old image file: " + imagePath);
            }
        } catch (Exception e) {
            System.out.println("Failed to delete old image file: " + e.getMessage());
        }
    }

    private boolean isValidForCreate(Product product) {
        if (product == null) return false;
        if (isBlank(product.getCategoryId())) return false;
        if (isBlank(product.getName())) return false;
        if (product.getName().length() > 200) return false;
        if (product.getBasePrice() == null || product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) return false;
        if (!Product.isValidStatus(product.getStatus())) return false;
        if (product.getVariants() == null || product.getVariants().isEmpty()) return false;
        return isVariantsValid(product.getVariants());
    }

    private boolean isValidForUpdate(Product product) {
        if (!isValidForCreate(product)) return false;
        return product.getProductId() != null && !product.getProductId().isBlank();
    }

    private boolean isVariantsValid(List<ProductVariant> variants) {
        for (ProductVariant variant : variants) {
            if (isBlank(variant.getSizeId())) return false;
            if (isBlank(variant.getColorId())) return false;
            if (variant.getStockQty() < 0) return false;
            if (variant.getPriceOverride() != null && variant.getPriceOverride().compareTo(BigDecimal.ZERO) < 0) return false;
        }
        return true;
    }

    private boolean isBlank(String str) { return str == null || str.isBlank(); }

    public boolean isDatabaseReady() { return productDAO.isDatabaseReady(); }
    public BigDecimal getDisplayPrice(Product product) { return productDAO.getDisplayPrice(product); }
}
