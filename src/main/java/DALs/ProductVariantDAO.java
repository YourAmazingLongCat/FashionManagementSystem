package DALs;

import Models.ProductVariant;
import Utils.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductVariantDAO extends DBContext {

    public ProductVariantDAO() {
        super();
    }

    public List<ProductVariant> getVariantsByProductId(String productId) {
        List<ProductVariant> variants = new ArrayList<>();
        if (productId == null || productId.isBlank()) {
            return variants;
        }

        String sql = "SELECT pv.variantId, pv.productId, pv.sizeId, s.sizeName, pv.colorId, c.colorName, c.hexCode, pv.sku, pv.stockQty, pv.priceOverride "
                + "FROM ProductVariants pv "
                + "INNER JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "INNER JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE pv.productId = ? "
                + "ORDER BY c.colorName, s.sizeName";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    variants.add(variant);
                }
            }
        } catch (SQLException e) {
            System.out.println("getVariantsByProductId error: " + e.getMessage());
        }

        return variants;
    }

    public boolean replaceVariants(String productId, List<ProductVariant> variants) {
        if (productId == null || productId.isBlank()) {
            return false;
        }

        String getOldSql = "SELECT variantId FROM ProductVariants WHERE productId = ?";
        String deleteCartSql = "DELETE FROM CartItems WHERE variantId = ?";
        String deleteSql = "DELETE FROM ProductVariants WHERE productId = ?";
        String insertSql = "INSERT INTO ProductVariants (variantId, productId, sizeId, colorId, sku, stockQty, priceOverride, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())";

        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);

            try {
                List<String> oldVariantIds = new ArrayList<>();

                try (PreparedStatement psGet = conn.prepareStatement(getOldSql)) {
                    psGet.setString(1, productId);
                    try (ResultSet rs = psGet.executeQuery()) {
                        while (rs.next()) {
                            oldVariantIds.add(rs.getString("variantId"));
                        }
                    }
                }

                try (PreparedStatement psDeleteCart = conn.prepareStatement(deleteCartSql)) {
                    for (String variantId : oldVariantIds) {
                        psDeleteCart.setString(1, variantId);
                        psDeleteCart.executeUpdate();
                    }
                }

                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setString(1, productId);
                    psDelete.executeUpdate();
                }

                if (variants != null) {
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        for (ProductVariant variant : variants) {
                            if (variant == null || variant.getSizeId() == null || variant.getSizeId().isBlank()
                                    || variant.getColorId() == null || variant.getColorId().isBlank()) {
                                continue;
                            }
                            psInsert.setString(1, generateVariantId());
                            psInsert.setString(2, productId);
                            psInsert.setString(3, variant.getSizeId());
                            psInsert.setString(4, variant.getColorId());
                            psInsert.setString(5, variant.getSku());
                            psInsert.setInt(6, Math.max(0, variant.getStockQty()));
                            if (variant.getPriceOverride() != null) {
                                psInsert.setBigDecimal(7, variant.getPriceOverride());
                            } else {
                                psInsert.setNull(7, java.sql.Types.DECIMAL);
                            }
                            psInsert.executeUpdate();
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("replaceVariants error: " + e.getMessage());
            return false;
        }
    }

    public int getTotalStockQty(String productId) {
        if (productId == null || productId.isBlank()) {
            return 0;
        }

        String sql = "SELECT ISNULL(SUM(stockQty), 0) AS totalStockQty FROM ProductVariants WHERE productId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalStockQty");
                }
            }
        } catch (SQLException e) {
            System.out.println("getTotalStockQty error: " + e.getMessage());
        }

        return 0;
    }

    private String generateVariantId() {
        return "VAR" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    /**
     * Deduct stock for a single variant by a given quantity.
     * Used when staff confirms an order to reduce warehouse inventory.
     *
     * @param variantId the variant to deduct from
     * @param quantity  the quantity to deduct
     * @return true if successful
     */
    public boolean deductStock(String variantId, int quantity) {
        if (variantId == null || variantId.isBlank() || quantity <= 0) {
            return false;
        }

        String sql = "UPDATE ProductVariants SET stockQty = stockQty - ? WHERE variantId = ? AND stockQty >= ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, variantId);
            ps.setInt(3, quantity);
            int updated = ps.executeUpdate();
            System.out.println("deductStock: variantId=" + variantId + ", qty=" + quantity + ", rowsUpdated=" + updated);
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("deductStock error for variantId=" + variantId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
