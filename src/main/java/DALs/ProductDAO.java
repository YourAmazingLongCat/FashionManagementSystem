package DALs;

import Models.Product;
import Models.ProductVariant;
import Utils.DBContext;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductDAO extends DBContext {

    public ProductDAO() {
        super();
    }

    public boolean isDatabaseReady() {
        return connection != null;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady()) {
            System.out.println("getAllProducts: database not ready");
            return products;
        }

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            ORDER BY p.createdAt DESC
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllProducts error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("getAllProducts: loaded " + products.size() + " products");
        loadVariantsForProducts(products);
        return products;
    }

    public ProductResult getProductsFiltered(String keyword, String status, String categoryId,
                                             int page, int pageSize) {
        return getProductsFiltered(keyword, status, categoryId, null, null, null, page, pageSize);
    }

    public ProductResult getProductsFiltered(String keyword, String status, String categoryId,
                                             String skuFilter, String sizeFilter, String colorFilter,
                                             int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        int totalCount = 0;

        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        System.out.println("getProductsFiltered: page=" + page + ", pageSize=" + pageSize);

        if (!isDatabaseReady()) {
            System.out.println("getProductsFiltered: database not ready");
            return new ProductResult(products, 0);
        }

        totalCount = countProducts(keyword, status, categoryId, skuFilter, sizeFilter, colorFilter);
        System.out.println("getProductsFiltered: totalCount = " + totalCount);

        if (totalCount == 0) {
            return new ProductResult(products, 0);
        }

        String sql = buildPaginatedQuery(keyword, status, categoryId, skuFilter, sizeFilter, colorFilter);
        int offset = (page - 1) * pageSize;
        int limit = pageSize;

        System.out.println("getProductsFiltered: executing query with limit=" + limit + ", offset=" + offset);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int paramIndex = setQueryParameters(ps, keyword, status, categoryId, skuFilter, sizeFilter, colorFilter);
            ps.setInt(paramIndex++, offset);
            ps.setInt(paramIndex++, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getProductsFiltered error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("getProductsFiltered: loaded " + products.size() + " products for page " + page);
        loadVariantsForProducts(products);
        return new ProductResult(products, totalCount);
    }

    private int countProducts(String keyword, String status, String categoryId) {
        return countProducts(keyword, status, categoryId, null, null, null);
    }

    private int countProducts(String keyword, String status, String categoryId,
                             String skuFilter, String sizeFilter, String colorFilter) {
        String sql = buildCountQuery(keyword, status, categoryId, skuFilter, sizeFilter, colorFilter);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setQueryParameters(ps, keyword, status, categoryId, skuFilter, sizeFilter, colorFilter);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalCount");
                }
            }
        } catch (SQLException e) {
            System.out.println("countProducts error: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private String buildCountQuery(String keyword, String status, String categoryId) {
        return buildCountQuery(keyword, status, categoryId, null, null, null);
    }

    private String buildCountQuery(String keyword, String status, String categoryId,
                                   String skuFilter, String sizeFilter, String colorFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT p.productId) AS totalCount FROM Products p ");
        sql.append("INNER JOIN Categories c ON p.categoryId = c.categoryId ");

        boolean hasVariantFilter = (skuFilter != null && !skuFilter.isBlank())
                                || (sizeFilter != null && !sizeFilter.isBlank())
                                || (colorFilter != null && !colorFilter.isBlank());

        if (hasVariantFilter) {
            sql.append("INNER JOIN ProductVariants pv ON p.productId = pv.productId ");
            if (sizeFilter != null && !sizeFilter.isBlank()) {
                sql.append("INNER JOIN Sizes s ON pv.sizeId = s.sizeId ");
            }
            if (colorFilter != null && !colorFilter.isBlank()) {
                sql.append("INNER JOIN Colors cl ON pv.colorId = cl.colorId ");
            }
        }

        sql.append("WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(c.name) LIKE ? ");
            sql.append("OR LOWER(pv.sku) LIKE ? OR LOWER(s.sizeName) LIKE ? OR LOWER(cl.colorName) LIKE ?) ");
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND p.status = ? ");
        }
        if (categoryId != null && !categoryId.isBlank()) {
            sql.append("AND p.categoryId = ? ");
        }
        if (skuFilter != null && !skuFilter.isBlank()) {
            sql.append("AND LOWER(pv.sku) LIKE ? ");
        }
        if (sizeFilter != null && !sizeFilter.isBlank()) {
            sql.append("AND s.sizeId = ? ");
        }
        if (colorFilter != null && !colorFilter.isBlank()) {
            sql.append("AND cl.colorId = ? ");
        }

        return sql.toString();
    }

    private String buildPaginatedQuery(String keyword, String status, String categoryId) {
        return buildPaginatedQuery(keyword, status, categoryId, null, null, null);
    }

    private String buildPaginatedQuery(String keyword, String status, String categoryId,
                                       String skuFilter, String sizeFilter, String colorFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT * FROM (
                SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                       p.basePrice, p.status, p.createdAt, p.updatedAt,
                       (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl,
                       ROW_NUMBER() OVER (ORDER BY p.createdAt DESC) AS rowNum
                FROM Products p
                INNER JOIN Categories c ON p.categoryId = c.categoryId
            """);

        boolean hasVariantFilter = (skuFilter != null && !skuFilter.isBlank())
                                || (sizeFilter != null && !sizeFilter.isBlank())
                                || (colorFilter != null && !colorFilter.isBlank());

        if (hasVariantFilter) {
            sql.append("INNER JOIN ProductVariants pv ON p.productId = pv.productId ");
            if (sizeFilter != null && !sizeFilter.isBlank()) {
                sql.append("INNER JOIN Sizes s ON pv.sizeId = s.sizeId ");
            }
            if (colorFilter != null && !colorFilter.isBlank()) {
                sql.append("INNER JOIN Colors cl ON pv.colorId = cl.colorId ");
            }
        }

        sql.append("WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(c.name) LIKE ? ");
            sql.append("OR LOWER(pv.sku) LIKE ? OR LOWER(s.sizeName) LIKE ? OR LOWER(cl.colorName) LIKE ?) ");
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND p.status = ? ");
        }
        if (categoryId != null && !categoryId.isBlank()) {
            sql.append("AND p.categoryId = ? ");
        }
        if (skuFilter != null && !skuFilter.isBlank()) {
            sql.append("AND LOWER(pv.sku) LIKE ? ");
        }
        if (sizeFilter != null && !sizeFilter.isBlank()) {
            sql.append("AND s.sizeId = ? ");
        }
        if (colorFilter != null && !colorFilter.isBlank()) {
            sql.append("AND cl.colorId = ? ");
        }

        sql.append("""
                ) AS productsWithRowNum
                WHERE rowNum > ? AND rowNum <= ?
                """);

        return sql.toString();
    }

    private int setQueryParameters(PreparedStatement ps, String keyword, String status, String categoryId) throws SQLException {
        return setQueryParameters(ps, keyword, status, categoryId, null, null, null);
    }

    private int setQueryParameters(PreparedStatement ps, String keyword, String status, String categoryId,
                                   String skuFilter, String sizeFilter, String colorFilter) throws SQLException {
        int paramIndex = 1;

        if (keyword != null && !keyword.isBlank()) {
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);
        }

        if (status != null && !status.isBlank()) {
            ps.setString(paramIndex++, status);
        }

        if (categoryId != null && !categoryId.isBlank()) {
            ps.setString(paramIndex++, categoryId);
        }

        if (skuFilter != null && !skuFilter.isBlank()) {
            ps.setString(paramIndex++, "%" + skuFilter.toLowerCase() + "%");
        }
        if (sizeFilter != null && !sizeFilter.isBlank()) {
            ps.setString(paramIndex++, sizeFilter);
        }
        if (colorFilter != null && !colorFilter.isBlank()) {
            ps.setString(paramIndex++, colorFilter);
        }

        return paramIndex;
    }

    public List<Product> getProductsByCategoryName(String categoryName, int limit) {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady() || categoryName == null || categoryName.isBlank() || limit <= 0) {
            return products;
        }

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
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
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getProductsByCategoryName error: " + e.getMessage());
        }

        loadVariantsForProducts(products);
        return products;
    }

    public Product getProductById(String productId) {
        if (!isDatabaseReady() || productId == null || productId.isBlank()) {
            return null;
        }

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE p.productId = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product product = mapProduct(rs);

                    List<ProductVariant> variants = loadVariantsForProduct(productId);
                    product.setVariants(variants);

                    int totalStock = 0;
                    List<String> sizeIds = new ArrayList<>();
                    List<String> colorIds = new ArrayList<>();
                    List<String> sizeNames = new ArrayList<>();
                    List<String> colorNames = new ArrayList<>();

                    for (ProductVariant v : variants) {
                        totalStock += v.getStockQty();
                        if (v.getSizeId() != null && !sizeIds.contains(v.getSizeId())) {
                            sizeIds.add(v.getSizeId());
                            sizeNames.add(v.getSizeName());
                        }
                        if (v.getColorId() != null && !colorIds.contains(v.getColorId())) {
                            colorIds.add(v.getColorId());
                            colorNames.add(v.getColorName());
                        }
                    }

                    product.setTotalStockQty(totalStock);
                    product.setSizeIds(sizeIds);
                    product.setColorIds(colorIds);
                    product.setSizeNames(sizeNames);
                    product.setColorNames(colorNames);

                    return product;
                }
            }
        } catch (SQLException e) {
            System.out.println("getProductById error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Product> getLatestProducts(int limit) {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady() || limit <= 0) {
            return products;
        }

        String sql = """
            SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description,
                   p.basePrice, p.status, p.createdAt, p.updatedAt,
                   (SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = p.productId ORDER BY isPrimary DESC, imageId ASC) AS primaryImageUrl
            FROM Products p
            INNER JOIN Categories c ON p.categoryId = c.categoryId
            WHERE p.status = 'Available'
            ORDER BY p.createdAt DESC
            OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getLatestProducts error: " + e.getMessage());
        }

        loadVariantsForProducts(products);
        return products;
    }

    private List<ProductVariant> loadVariantsForProduct(String productId) {
        List<ProductVariant> variants = new ArrayList<>();

        String sql = """
            SELECT pv.variantId, pv.productId, pv.sizeId, s.sizeName, pv.colorId, cl.colorName, cl.hexCode,
                   pv.sku, pv.stockQty, pv.priceOverride
            FROM ProductVariants pv
            INNER JOIN Sizes s ON pv.sizeId = s.sizeId
            INNER JOIN Colors cl ON pv.colorId = cl.colorId
            WHERE pv.productId = ?
            ORDER BY cl.colorName, s.sizeName
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    variants.add(mapVariant(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("loadVariantsForProduct error: " + e.getMessage());
        }

        return variants;
    }

    private void loadVariantsForProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        List<String> productIds = new ArrayList<>();
        for (Product p : products) {
            if (p.getProductId() != null) {
                productIds.add(p.getProductId());
            }
        }

        if (productIds.isEmpty()) {
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = String.format("""
            SELECT pv.productId, pv.variantId, pv.sizeId, s.sizeName, pv.colorId, cl.colorName, cl.hexCode,
                   pv.sku, pv.stockQty, pv.priceOverride
            FROM ProductVariants pv
            INNER JOIN Sizes s ON pv.sizeId = s.sizeId
            INNER JOIN Colors cl ON pv.colorId = cl.colorId
            WHERE pv.productId IN (%s)
            ORDER BY pv.productId, cl.colorName, s.sizeName
            """, placeholders.toString());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                ps.setString(i + 1, productIds.get(i));
            }

            Map<String, Integer> stockMap = new HashMap<>();
            Map<String, List<String>> sizeIdMap = new HashMap<>();
            Map<String, List<String>> colorIdMap = new HashMap<>();
            Map<String, List<String>> sizeNameMap = new HashMap<>();
            Map<String, List<String>> colorNameMap = new HashMap<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pid = rs.getString("productId");
                    String sizeId = rs.getString("sizeId");
                    String sizeName = rs.getString("sizeName");
                    String colorId = rs.getString("colorId");
                    String colorName = rs.getString("colorName");

                    int currentStock = stockMap.getOrDefault(pid, 0);
                    stockMap.put(pid, currentStock + rs.getInt("stockQty"));

                    sizeIdMap.computeIfAbsent(pid, k -> new ArrayList<>());
                    if (sizeId != null && !sizeIdMap.get(pid).contains(sizeId)) {
                        sizeIdMap.get(pid).add(sizeId);
                        sizeNameMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(sizeName);
                    }

                    colorIdMap.computeIfAbsent(pid, k -> new ArrayList<>());
                    if (colorId != null && !colorIdMap.get(pid).contains(colorId)) {
                        colorIdMap.get(pid).add(colorId);
                        colorNameMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(colorName);
                    }
                }
            }

            for (Product p : products) {
                String pid = p.getProductId();
                p.setTotalStockQty(stockMap.getOrDefault(pid, 0));
                p.setSizeIds(sizeIdMap.getOrDefault(pid, new ArrayList<>()));
                p.setColorIds(colorIdMap.getOrDefault(pid, new ArrayList<>()));
                p.setSizeNames(sizeNameMap.getOrDefault(pid, new ArrayList<>()));
                p.setColorNames(colorNameMap.getOrDefault(pid, new ArrayList<>()));
            }
        } catch (SQLException e) {
            System.out.println("loadVariantsForProducts error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean createProduct(Product product) {
        if (!isDatabaseReady() || product == null) {
            return false;
        }

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
        } catch (SQLException e) {
            System.out.println("createProduct error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateProduct(Product product) {
        if (!isDatabaseReady() || product == null || product.getProductId() == null) {
            return false;
        }

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
        } catch (SQLException e) {
            System.out.println("updateProduct error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteProduct(String productId) {
        if (!isDatabaseReady() || productId == null || productId.isBlank()) {
            return false;
        }

        // Delete related Wishlists first
        String deleteWishlists = "DELETE FROM Wishlists WHERE productId = ?";
        try (PreparedStatement psWishlist = connection.prepareStatement(deleteWishlists)) {
            psWishlist.setString(1, productId);
            psWishlist.executeUpdate();
        } catch (SQLException e) {
            System.out.println("deleteProduct - deleteWishlists error: " + e.getMessage());
        }

        // Delete related CartItems
        String deleteCartItems = "DELETE FROM CartItems WHERE variantId IN (SELECT variantId FROM ProductVariants WHERE productId = ?)";
        try (PreparedStatement psCart = connection.prepareStatement(deleteCartItems)) {
            psCart.setString(1, productId);
            psCart.executeUpdate();
        } catch (SQLException e) {
            System.out.println("deleteProduct - deleteCartItems error: " + e.getMessage());
        }

        // Delete ProductVariants
        String deleteVariants = "DELETE FROM ProductVariants WHERE productId = ?";
        try (PreparedStatement psVariant = connection.prepareStatement(deleteVariants)) {
            psVariant.setString(1, productId);
            psVariant.executeUpdate();
        } catch (SQLException e) {
            System.out.println("deleteProduct - deleteVariants error: " + e.getMessage());
        }

        // Delete ProductImages
        String deleteImages = "DELETE FROM ProductImages WHERE productId = ?";
        try (PreparedStatement psImage = connection.prepareStatement(deleteImages)) {
            psImage.setString(1, productId);
            psImage.executeUpdate();
        } catch (SQLException e) {
            System.out.println("deleteProduct - deleteImages error: " + e.getMessage());
        }

        // Finally delete the Product
        String sql = "DELETE FROM Products WHERE productId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteProduct error: " + e.getMessage());
        }

        return false;
    }

    public BigDecimal getDisplayPrice(Product product) {
        if (product == null || product.getVariants() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal minPrice = product.getBasePrice();
        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
        }

        for (ProductVariant variant : product.getVariants()) {
            if (variant != null && variant.getPriceOverride() != null
                    && variant.getPriceOverride().compareTo(minPrice) < 0) {
                minPrice = variant.getPriceOverride();
            }
        }

        return minPrice;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
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

    private ProductVariant mapVariant(ResultSet rs) throws SQLException {
        ProductVariant variant = new ProductVariant();
        variant.setVariantId(rs.getString("variantId"));
        variant.setProductId(rs.getString("productId"));
        variant.setSizeId(rs.getString("sizeId"));
        variant.setSizeName(rs.getString("sizeName"));
        variant.setColorId(rs.getString("colorId"));
        variant.setColorName(rs.getString("colorName"));
        variant.setColorHexCode(rs.getString("hexCode"));
        variant.setSku(rs.getString("sku"));
        variant.setStockQty(rs.getInt("stockQty"));
        variant.setPriceOverride(rs.getBigDecimal("priceOverride"));
        return variant;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private String generateProductId() {
        return "PRD" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    public ProductVariant getVariantById(String variantId) {
        if (variantId == null || !isDatabaseReady()) return null;

        String sql = """
            SELECT pv.*, p.basePrice
            FROM ProductVariants pv
            JOIN Products p ON pv.productId = p.productId
            WHERE pv.variantId = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductVariant variant = new ProductVariant();
                    variant.setVariantId(rs.getString("variantId"));
                    variant.setProductId(rs.getString("productId"));
                    variant.setSizeId(rs.getString("sizeId"));
                    variant.setColorId(rs.getString("colorId"));
                    variant.setSku(rs.getString("sku"));
                    variant.setStockQty(rs.getInt("stockQty"));
                    variant.setPriceOverride(rs.getBigDecimal("priceOverride"));
                    return variant;
                }
            }
        } catch (SQLException e) {
            System.out.println("getVariantById error: " + e.getMessage());
        }
        return null;
    }

    public record ProductResult(List<Product> products, int totalCount) {
        public int totalPages(int pageSize) {
            return (int) Math.ceil((double) totalCount / pageSize);
        }
    }
}
