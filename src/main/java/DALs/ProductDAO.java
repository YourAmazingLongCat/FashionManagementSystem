package DALs;

import Models.Product;
import Models.ProductVariant;
import java.math.BigDecimal;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Product DAO handling CRUD operations for Products table.
 */
public class ProductDAO extends DBContext {

    public ProductDAO() {
        super();
    }

    public boolean isDatabaseReady() {
        return connection != null;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description, "
                + "p.basePrice, p.status, p.createdAt, p.updatedAt, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS primaryImageUrl "
                + "FROM Products p "
                + "INNER JOIN Categories c ON p.categoryId = c.categoryId "
                + "ORDER BY p.createdAt DESC";

        if (!isDatabaseReady()) {
            System.out.println("getAllProducts error: database connection is not available.");
            return products;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product product = mapProduct(rs);
                loadProductVariants(product);
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("getAllProducts error: " + e.getMessage());
        }

        return products;
    }

    public Product getProductById(String productId) {
        String sql = "SELECT p.productId, p.categoryId, c.name AS categoryName, p.name, p.description, "
                + "p.basePrice, p.status, p.createdAt, p.updatedAt, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS primaryImageUrl "
                + "FROM Products p "
                + "INNER JOIN Categories c ON p.categoryId = c.categoryId "
                + "WHERE p.productId = ?";

        if (!isDatabaseReady()) {
            System.out.println("getProductById error: database connection is not available.");
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product product = mapProduct(rs);
                    loadProductVariants(product);
                    return product;
                }
            }
        } catch (SQLException e) {
            System.out.println("getProductById error: " + e.getMessage());
        }

        return null;
    }

    public boolean createProduct(Product product) {
        String sql = "INSERT INTO Products (productId, categoryId, name, description, basePrice, status, createdAt, updatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

        if (product.getProductId() == null || product.getProductId().isBlank()) {
            product.setProductId(generateProductId());
        }

        if (!isDatabaseReady()) {
            System.out.println("createProduct error: database connection is not available.");
            return false;
        }

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
        }

        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE Products SET categoryId = ?, name = ?, description = ?, "
                + "basePrice = ?, status = ?, updatedAt = GETDATE() WHERE productId = ?";

        if (!isDatabaseReady()) {
            System.out.println("updateProduct error: database connection is not available.");
            return false;
        }

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
        }

        return false;
    }

    public boolean deleteProduct(String productId) {
        String sql = "DELETE FROM Products WHERE productId = ?";

        if (!isDatabaseReady()) {
            System.out.println("deleteProduct error: database connection is not available.");
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteProduct error: " + e.getMessage());
        }

        return false;
    }

    public List<Product> getLatestProducts(int limit) {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady() || limit <= 0) {
            return products;
        }

        String sql = "SELECT TOP " + limit + " p.productId, p.categoryId, c.name AS categoryName, p.name, p.description, "
                + "p.basePrice, p.status, p.createdAt, p.updatedAt, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS primaryImageUrl "
                + "FROM Products p "
                + "INNER JOIN Categories c ON p.categoryId = c.categoryId "
                + "WHERE p.status = 'Available' "
                + "ORDER BY p.createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product product = mapProduct(rs);
                loadProductVariants(product);
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("getLatestProducts error: " + e.getMessage());
        }

        return products;
    }

    public List<Product> getProductsByCategoryName(String categoryName, int limit) {
        List<Product> products = new ArrayList<>();
        if (!isDatabaseReady() || categoryName == null || categoryName.isBlank() || limit <= 0) {
            return products;
        }

        String sql = "SELECT TOP " + limit + " p.productId, p.categoryId, c.name AS categoryName, p.name, p.description, "
                + "p.basePrice, p.status, p.createdAt, p.updatedAt, "
                + "(SELECT TOP 1 pi.imageUrl FROM ProductImages pi WHERE pi.productId = p.productId ORDER BY pi.isPrimary DESC, pi.imageId ASC) AS primaryImageUrl "
                + "FROM Products p "
                + "INNER JOIN Categories c ON p.categoryId = c.categoryId "
                + "WHERE p.status = 'Available' AND c.name = ? "
                + "ORDER BY p.createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = mapProduct(rs);
                    loadProductVariants(product);
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.out.println("getProductsByCategoryName error: " + e.getMessage());
        }

        return products;
    }

    public BigDecimal getDisplayPrice(Product product) {
        if (product == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal minPrice = product.getBasePrice();
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant == null || variant.getPriceOverride() == null) {
                    continue;
                }
                if (minPrice == null || variant.getPriceOverride().compareTo(minPrice) < 0) {
                    minPrice = variant.getPriceOverride();
                }
            }
        }
        return minPrice != null ? minPrice : BigDecimal.ZERO;
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

    private void loadProductVariants(Product product) {
        if (product == null || product.getProductId() == null || product.getProductId().isBlank()) {
            return;
        }

        ProductVariantDAO variantDAO = new ProductVariantDAO();
        List<ProductVariant> variants = variantDAO.getVariantsByProductId(product.getProductId());
        product.setVariants(variants);
        product.setTotalStockQty(variantDAO.getTotalStockQty(product.getProductId()));

        Set<String> sizeIds = new LinkedHashSet<>();
        Set<String> sizeNames = new LinkedHashSet<>();
        Set<String> colorIds = new LinkedHashSet<>();
        Set<String> colorNames = new LinkedHashSet<>();

        for (ProductVariant variant : variants) {
            if (variant.getSizeId() != null) {
                sizeIds.add(variant.getSizeId());
            }
            if (variant.getSizeName() != null) {
                sizeNames.add(variant.getSizeName());
            }
            if (variant.getColorId() != null) {
                colorIds.add(variant.getColorId());
            }
            if (variant.getColorName() != null) {
                colorNames.add(variant.getColorName());
            }
        }

        product.setSizeIds(new ArrayList<>(sizeIds));
        product.setSizeNames(new ArrayList<>(sizeNames));
        product.setColorIds(new ArrayList<>(colorIds));
        product.setColorNames(new ArrayList<>(colorNames));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private String generateProductId() {
        return "PRD" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
