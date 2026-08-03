package DALs;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Models.Product;
import Models.ProductVariant;
import Utils.DBContext;


public class ProductDAO extends DBContext {

    public ProductDAO() {
        super();
    }

    // ============ Read ============

    public boolean isDatabaseReady() {
        return connection != null;
    }

    /**
     * Get all products (used by Home, Dashboard, Warehouse...).
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady()) return products;

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId
                    ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            ORDER BY p.createdAt DESC
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) products.add(mapProduct(rs));
        } catch (Exception e) {
            System.out.println("getAllProducts error: " + e.getMessage());
        }

        loadVariantsForProducts(products);
        return products;
    }

    /**
     * Get display price (min of basePrice and priceOverride).
     */
    public BigDecimal getDisplayPrice(Product product) {
        if (product == null) return BigDecimal.ZERO;
        BigDecimal minPrice = product.getBasePrice();
        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {
                if (v != null && v.getPriceOverride() != null
                        && v.getPriceOverride().compareTo(minPrice) < 0) {
                    minPrice = v.getPriceOverride();
                }
            }
        }
        return minPrice;
    }

    /**
     * Get products with pagination and filters.
     */
    public ProductResult getProductsFiltered(String keyword, String status, String categoryId,
                                             int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        int totalCount = 0;

        if (!isDatabaseReady()) return new ProductResult(products, 0);
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;

        // Count matching products
        String countSql = """
            SELECT COUNT(*) AS totalCount FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE 1=1
            """ + buildWhereClause(keyword, status, categoryId);

        try (PreparedStatement ps = connection.prepareStatement(countSql)) {
            setWhereParams(ps, keyword, status, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalCount = rs.getInt("totalCount");
            }
        } catch (Exception e) {
            System.out.println("countProducts error: " + e.getMessage());
        }

        if (totalCount == 0) return new ProductResult(products, 0);

        // Get products by page
        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId
                    ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE 1=1
            """ + buildWhereClause(keyword, status, categoryId) + """
            ORDER BY p.createdAt DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

        int offset = (page - 1) * pageSize;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = setWhereParams(ps, keyword, status, categoryId);
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) products.add(mapProduct(rs));
            }
        } catch (Exception e) {
            System.out.println("getProductsFiltered error: " + e.getMessage());
        }

        loadVariantsForProducts(products);
        return new ProductResult(products, totalCount);
    }

    private String buildWhereClause(String keyword, String status, String categoryId) {
        StringBuilder sb = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) {
            sb.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(c.name) LIKE ? ")
              .append(" OR EXISTS (SELECT 1 FROM ProductVariants pv ")
              .append("LEFT JOIN Colors co ON pv.colorId = co.colorId ")
              .append("LEFT JOIN Sizes s ON pv.sizeId = s.sizeId ")
              .append("WHERE pv.productId = p.productId ")
              .append("AND (LOWER(ISNULL(pv.sku, '')) LIKE ? OR LOWER(ISNULL(co.colorName, '')) LIKE ? OR LOWER(ISNULL(s.sizeName, '')) LIKE ?))) ");
        }
        if (status != null && !status.isBlank()) sb.append(" AND p.status = ? ");
        if (categoryId != null && !categoryId.isBlank()) sb.append(" AND p.categoryId = ? ");
        return sb.toString();
    }

    private int setWhereParams(PreparedStatement ps, String keyword, String status, String categoryId) throws Exception {
        int idx = 1;
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.toLowerCase() + "%";
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
        }
        if (status != null && !status.isBlank()) ps.setString(idx++, status);
        if (categoryId != null && !categoryId.isBlank()) ps.setString(idx++, categoryId);
        return idx;
    }

    public Product getProductById(String productId) {
        if (!isDatabaseReady() || productId == null || productId.isBlank()) return null;

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId
                    ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE p.productId = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product product = mapProduct(rs);
                    product.setVariants(loadVariants(productId));
                    populateDerivedFields(product);
                    return product;
                }
            }
        } catch (Exception e) {
            System.out.println("getProductById error: " + e.getMessage());
        }
        return null;
    }

    public List<Product> getLatestProducts(int limit) {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady() || limit <= 0) return products;

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId
                    ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE p.status = 'Available'
            ORDER BY p.createdAt DESC
            OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) products.add(mapProduct(rs));
            }
        } catch (Exception e) {
            System.out.println("getLatestProducts error: " + e.getMessage());
        }

        loadVariantsForProducts(products);
        return products;
    }

    /**
     * Get products by category name (used for Home recommendations).
     */
    public List<Product> getProductsByCategoryName(String categoryName, int limit) {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady() || categoryName == null || categoryName.isBlank() || limit <= 0) {
            return products;
        }

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId
                    ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE p.status = 'Available' AND c.name = ?
            ORDER BY p.createdAt DESC
            OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) products.add(mapProduct(rs));
            }
        } catch (Exception e) {
            System.out.println("getProductsByCategoryName error: " + e.getMessage());
        }

        loadVariantsForProducts(products);
        return products;
    }

    /**
     * Get a variant by variantId (used for Cart).
     */
    public ProductVariant getVariantById(String variantId) {
        if (variantId == null || variantId.isBlank() || !isDatabaseReady()) return null;

        String sql = """
            SELECT pv.variantId, pv.productId, pv.sizeId, s.sizeName, pv.colorId, cl.colorName, cl.hexCode,
                   pv.sku, pv.stockQty, pv.reservedQty, pv.priceOverride
            FROM ProductVariants pv
            INNER JOIN Sizes s ON pv.sizeId = s.sizeId
            INNER JOIN Colors cl ON pv.colorId = cl.colorId
            WHERE pv.variantId = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapVariant(rs);
            }
        } catch (Exception e) {
            System.out.println("getVariantById error: " + e.getMessage());
        }
        return null;
    }

    // ============ Write ============

    public boolean createProduct(Product product) {
        if (!isDatabaseReady() || product == null) return false;

        if (product.getProductId() == null || product.getProductId().isBlank()) {
            product.setProductId(generateProductId());
        }

        String sql = """
            INSERT INTO Products (productId, categoryId, name, description, basePrice, status, createdAt, updatedAt)
            VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getProductId());
            ps.setString(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getBasePrice());
            ps.setString(6, product.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("createProduct error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Count products using this category (check before deleting Category).
     */
    public int countByCategoryId(String categoryId) {
        if (!isDatabaseReady() || categoryId == null || categoryId.isBlank()) return 0;
        String sql = "SELECT COUNT(*) FROM Products WHERE categoryId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ex) {
            System.out.println("countByCategoryId error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Update product. Staff can edit anything.
     */
    public boolean updateProduct(Product product) {
        if (!isDatabaseReady() || product == null || product.getProductId() == null) return false;

        String sql = """
            UPDATE Products
            SET categoryId = ?, name = ?, description = ?, basePrice = ?, status = ?, updatedAt = GETDATE()
            WHERE productId = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getBasePrice());
            ps.setString(5, product.getStatus());
            ps.setString(6, product.getProductId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("updateProduct error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete product. Staff can delete any product.
     * Wrapped in a transaction: any FK violation rolls back ALL deletes so the
     * product row, variants and image rows stay intact (avoids losing image
     * file when DB delete fails).
     *
     * Pending orders are blocked at the service layer (ProductService) before
     * we get here. For other order states (Confirmed, Processing, Shipping,
     * Delivered, Cancelled) we still need to clear OrderItems rows because
     * OrderItems.variantId is a hard FK to ProductVariants.
     *
     * WarehouseImports (stock-in history) referencing the variants are also
     * removed - per business rule the import history travels with the
     * product, so deleting the product clears those records too.
     */
    public boolean deleteProduct(String productId) {
        if (!isDatabaseReady() || productId == null || productId.isBlank()) return false;

        boolean prevAutoCommit = true;
        try {
            prevAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // Cleanup order matters - delete child rows that hold FK to
            // variants BEFORE deleting the variants themselves.
            executeUpdate("DELETE FROM Wishlists WHERE productId = ?", productId);
            executeUpdate("DELETE FROM CartItems WHERE variantId IN (SELECT variantId FROM ProductVariants WHERE productId = ?)", productId);
            // OrderItems for non-Pending orders: history rows that hard-FK to variants
            executeUpdate("DELETE FROM OrderItems WHERE variantId IN (SELECT variantId FROM ProductVariants WHERE productId = ?)", productId);
            // WarehouseImports (stock-in history) - FK to variants, removed along with the product
            executeUpdate("DELETE FROM WarehouseImports WHERE variantId IN (SELECT variantId FROM ProductVariants WHERE productId = ?)", productId);
            executeUpdate("DELETE FROM ProductVariants WHERE productId = ?", productId);
            executeUpdate("DELETE FROM ProductImages WHERE productId = ?", productId);
            executeUpdate("DELETE FROM Products WHERE productId = ?", productId);

            connection.commit();
            return getProductById(productId) == null;
        } catch (Exception e) {
            System.out.println("deleteProduct error: " + e.getMessage());
            try { connection.rollback(); } catch (Exception rb) { /* ignore */ }
            // Re-throw so service/controller layer can show a meaningful message
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(prevAutoCommit); } catch (Exception ignore) { /* ignore */ }
        }
    }

    private void executeUpdate(String sql, String productId) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("executeUpdate error: " + e.getMessage());
        }
    }

    // ============ Helper ============

    /**
     * After loading variants, sync derived fields:
     *   - sizeNames, colorNames (unique, sorted alpha)
     *   - totalStockQty = sum of availableQty (stock - reserved)
     */
    private void populateDerivedFields(Product product) {
        if (product == null) return;
        List<ProductVariant> variants = product.getVariants();
        if (variants == null || variants.isEmpty()) {
            product.setSizeNames(new ArrayList<>());
            product.setColorNames(new ArrayList<>());
            product.setTotalStockQty(0);
            return;
        }

        // Keep size/color ids for edit form
        java.util.LinkedHashSet<String> sizeIds = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> sizeNames = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> colorIds = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> colorNames = new java.util.LinkedHashSet<>();
        int totalStock = 0;

        for (ProductVariant v : variants) {
            if (v == null) continue;
            if (v.getSizeId() != null) sizeIds.add(v.getSizeId());
            if (v.getSizeName() != null) sizeNames.add(v.getSizeName());
            if (v.getColorId() != null) colorIds.add(v.getColorId());
            if (v.getColorName() != null) colorNames.add(v.getColorName());
            totalStock += v.getAvailableQty();
        }

        product.setSizeIds(new ArrayList<>(sizeIds));
        product.setSizeNames(new ArrayList<>(sizeNames));
        product.setColorIds(new ArrayList<>(colorIds));
        product.setColorNames(new ArrayList<>(colorNames));
        product.setTotalStockQty(totalStock);
    }

    private List<ProductVariant> loadVariants(String productId) {
        List<ProductVariant> variants = new ArrayList<>();
        String sql = """
            SELECT pv.variantId, pv.productId, pv.sizeId, s.sizeName, pv.colorId, cl.colorName, cl.hexCode,
                   pv.sku, pv.stockQty, pv.reservedQty, pv.priceOverride
            FROM ProductVariants pv
            INNER JOIN Sizes s ON pv.sizeId = s.sizeId
            INNER JOIN Colors cl ON pv.colorId = cl.colorId
            WHERE pv.productId = ?
            ORDER BY cl.colorName, s.sizeName
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) variants.add(mapVariant(rs));
            }
        } catch (Exception e) {
            System.out.println("loadVariants error: " + e.getMessage());
        }
        return variants;
    }

    private void loadVariantsForProducts(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        for (Product p : products) {
            if (p.getProductId() != null) {
                p.setVariants(loadVariants(p.getProductId()));
                populateDerivedFields(p);
            }
        }
    }

    private Product mapProduct(ResultSet rs) throws Exception {
        Product product = new Product();
        product.setProductId(rs.getString("productId"));
        product.setCategoryId(rs.getString("categoryId"));
        product.setCategoryName(rs.getString("categoryName"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setBasePrice(rs.getBigDecimal("basePrice"));
        product.setStatus(rs.getString("status"));
        product.setPrimaryImageUrl(rs.getString("primaryImageUrl"));
        product.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        product.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return product;
    }

    private ProductVariant mapVariant(ResultSet rs) throws Exception {
        ProductVariant v = new ProductVariant();
        v.setVariantId(rs.getString("variantId"));
        v.setProductId(rs.getString("productId"));
        v.setSizeId(rs.getString("sizeId"));
        v.setSizeName(rs.getString("sizeName"));
        v.setColorId(rs.getString("colorId"));
        v.setColorName(rs.getString("colorName"));
        v.setColorHexCode(rs.getString("hexCode"));
        v.setSku(rs.getString("sku"));
        v.setStockQty(rs.getInt("stockQty"));
        v.setReservedQty(rs.getInt("reservedQty"));
        v.setPriceOverride(rs.getBigDecimal("priceOverride"));
        return v;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private String generateProductId() {
        return "PRD" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    /**
     * Pagination result.
     */
    public static class ProductResult {
        private final List<Product> products;
        private final int totalCount;

        public ProductResult(List<Product> products, int totalCount) {
            this.products = products;
            this.totalCount = totalCount;
        }

        public List<Product> getProducts() { return products; }
        public int getTotalCount() { return totalCount; }
        public int getTotalPages(int pageSize) {
            return (int) Math.ceil((double) totalCount / pageSize);
        }
    }
}