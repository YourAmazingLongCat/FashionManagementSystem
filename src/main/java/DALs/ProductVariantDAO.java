package DALs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Models.ProductVariant;
import Utils.DBContext;

public class ProductVariantDAO extends DBContext {

    public ProductVariantDAO() {
        super();
    }

    public List<ProductVariant> getVariantsByProductId(String productId) {
        List<ProductVariant> variants = new ArrayList<>();
        if (productId == null || productId.isBlank()) {
            return variants;
        }

        String sql = "SELECT pv.variantId, pv.productId, pv.sizeId, s.sizeName, pv.colorId, c.colorName, "
                + "c.hexCode, pv.sku, pv.stockQty, pv.reservedQty, pv.priceOverride "
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
                    variant.setReservedQty(rs.getInt("reservedQty"));
                    variant.setPriceOverride(rs.getBigDecimal("priceOverride"));
                    variants.add(variant);
                }
            }
        } catch (SQLException e) {
            System.out.println("getVariantsByProductId error: " + e.getMessage());
        }

        return variants;
    }

    /**
     * Upsert variants for a product.
     *
     * Important: this method preserves existing variantIds whenever possible
     * so that warehouse import history and order history stay linked to the
     * same variant row. Behaviour:
     *   - For each incoming variant:
     *       * if its variantId exists in the DB  -> UPDATE fields, keep id.
     *       * otherwise (no id, or id not found) -> INSERT with a new id.
     *   - Old variants that are NOT present in the incoming list are removed,
     *     along with FK-cleanup rows in CartItems, OrderItems and their
     *     WarehouseImports history.
     *
     * This replaces the previous "wipe and re-insert" approach which lost
     * every WarehouseImports row of an existing product the moment the staff
     * re-saved the edit form.
     */
    public boolean replaceVariants(String productId, List<ProductVariant> variants) {
        if (productId == null || productId.isBlank()) return false;

        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);

            // 1. Load existing variants of the product.
            java.util.Map<String, ProductVariant> existingById = new java.util.HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT variantId, stockQty, reservedQty FROM ProductVariants WHERE productId = ?")) {
                ps.setString(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ProductVariant v = new ProductVariant();
                        v.setVariantId(rs.getString("variantId"));
                        v.setProductId(productId);
                        v.setStockQty(rs.getInt("stockQty"));
                        v.setReservedQty(rs.getInt("reservedQty"));
                        existingById.put(v.getVariantId(), v);
                    }
                }
            }

            java.util.Set<String> incomingIds = new java.util.HashSet<>();
            java.util.List<String> removedIds = new java.util.ArrayList<>();

            // 2. Partition incoming list: ids to keep vs new rows to insert.
            java.util.List<ProductVariant> toInsert = new java.util.ArrayList<>();
            java.util.List<ProductVariant> toUpdate = new java.util.ArrayList<>();
            if (variants != null) {
                for (ProductVariant v : variants) {
                    if (v == null) continue;
                    if (v.getSizeId() == null || v.getSizeId().isBlank()) continue;
                    if (v.getColorId() == null || v.getColorId().isBlank()) continue;
                    String id = v.getVariantId();
                    if (id != null && !id.isBlank() && existingById.containsKey(id)) {
                        toUpdate.add(v);
                        incomingIds.add(id);
                    } else {
                        toInsert.add(v);
                    }
                }
            }

            // 3. Anything in existingById but not in incomingIds is being removed.
            for (String existingId : existingById.keySet()) {
                if (!incomingIds.contains(existingId)) {
                    removedIds.add(existingId);
                }
            }

            // 4. Delete rows that reference removed variants. Order matters:
            //    clean FK targets first, then the variants themselves.
            if (!removedIds.isEmpty()) {
                deleteByVariantIds(conn,
                        "DELETE FROM CartItems WHERE variantId = ?",
                        removedIds);
                // WarehouseImports is history; we still remove it for variants
                // that are truly gone so the user doesn't see orphan imports.
                deleteByVariantIds(conn,
                        "DELETE FROM WarehouseImports WHERE variantId = ?",
                        removedIds);
                deleteByVariantIds(conn,
                        "DELETE FROM OrderItems WHERE variantId = ?",
                        removedIds);
                deleteByVariantIds(conn,
                        "DELETE FROM ProductVariants WHERE variantId = ?",
                        removedIds);
            }

            // 5. UPDATE existing variants (keep their variantId).
            if (!toUpdate.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ProductVariants SET sizeId = ?, colorId = ?, sku = ?, " +
                        "stockQty = ?, reservedQty = ?, priceOverride = ? " +
                        "WHERE variantId = ? AND productId = ?")) {
                    for (ProductVariant v : toUpdate) {
                        ps.setString(1, v.getSizeId());
                        ps.setString(2, v.getColorId());
                        ps.setString(3, v.getSku());
                        ps.setInt(4, Math.max(0, v.getStockQty()));
                        ps.setInt(5, Math.max(0, v.getReservedQty()));
                        if (v.getPriceOverride() != null) {
                            ps.setBigDecimal(6, v.getPriceOverride());
                        } else {
                            ps.setNull(6, java.sql.Types.DECIMAL);
                        }
                        ps.setString(7, v.getVariantId());
                        ps.setString(8, productId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // 6. INSERT brand-new variants with a fresh variantId.
            if (!toInsert.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ProductVariants (variantId, productId, sizeId, colorId, sku, " +
                        "stockQty, reservedQty, priceOverride, createdAt) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())")) {
                    for (ProductVariant v : toInsert) {
                        ps.setString(1, generateVariantId());
                        ps.setString(2, productId);
                        ps.setString(3, v.getSizeId());
                        ps.setString(4, v.getColorId());
                        ps.setString(5, v.getSku());
                        ps.setInt(6, Math.max(0, v.getStockQty()));
                        ps.setInt(7, Math.max(0, v.getReservedQty()));
                        if (v.getPriceOverride() != null) {
                            ps.setBigDecimal(8, v.getPriceOverride());
                        } else {
                            ps.setNull(8, java.sql.Types.DECIMAL);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            System.out.println("replaceVariants error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Delete rows by variantIds using batch.
     */
    private void deleteByVariantIds(Connection conn, String sql, List<String> variantIds) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String id : variantIds) {
                ps.setString(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int getTotalStockQty(String productId) {
        if (productId == null || productId.isBlank()) {
            return 0;
        }

        // Returns available stock (stockQty - reservedQty)
        String sql = "SELECT ISNULL(SUM(stockQty - reservedQty), 0) AS availableStock FROM ProductVariants WHERE productId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("availableStock");
                }
            }
        } catch (SQLException e) {
            System.out.println("getTotalStockQty error: " + e.getMessage());
        }

        return 0;
    }

    public int getTotalPhysicalStock(String productId) {
        if (productId == null || productId.isBlank()) {
            return 0;
        }

        // Returns physical stock (stockQty only)
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
            System.out.println("getTotalPhysicalStock error: " + e.getMessage());
        }

        return 0;
    }

    public int getAvailableStockByVariantId(String variantId) {
        if (variantId == null || variantId.isBlank()) {
            return 0;
        }

        String sql = "SELECT (stockQty - reservedQty) AS availableStock FROM ProductVariants WHERE variantId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("availableStock");
                }
            }
        } catch (SQLException e) {
            System.out.println("getAvailableStockByVariantId error: " + e.getMessage());
        }

        return 0;
    }

    public boolean reserveStock(String variantId, int quantity) {
        if (variantId == null || variantId.isBlank() || quantity <= 0) {
            return false;
        }

        String sql = "UPDATE ProductVariants SET reservedQty = reservedQty + ? "
                + "WHERE variantId = ? AND (stockQty - reservedQty) >= ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, variantId);
            ps.setInt(3, quantity);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("reserveStock error: " + e.getMessage());
        }

        return false;
    }

    public boolean releaseReservedStock(String variantId, int quantity) {
        if (variantId == null || variantId.isBlank() || quantity <= 0) {
            return false;
        }

        String sql = "UPDATE ProductVariants SET reservedQty = reservedQty - ? "
                + "WHERE variantId = ? AND reservedQty >= ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, variantId);
            ps.setInt(3, quantity);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("releaseReservedStock error: " + e.getMessage());
        }

        return false;
    }

    public boolean addStock(String variantId, int quantity) {
        if (variantId == null || variantId.isBlank() || quantity <= 0) {
            return false;
        }

        String sql = "UPDATE ProductVariants SET stockQty = stockQty + ? WHERE variantId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, variantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addStock error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Deduct physical stock for a single variant by a given quantity.
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

        // Deduct from physical stock, must have available stock (stockQty - reservedQty >= quantity)
        String sql = "UPDATE ProductVariants SET stockQty = stockQty - ? WHERE variantId = ? AND (stockQty - reservedQty) >= ?";

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

    /**
     * Count variants using this size (check before deleting Size).
     */
    public int countBySizeId(String sizeId) {
        if (sizeId == null || sizeId.isBlank()) return 0;
        String sql = "SELECT COUNT(*) FROM ProductVariants WHERE sizeId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sizeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("countBySizeId error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count variants using this color (check before deleting Color).
     */
    public int countByColorId(String colorId) {
        if (colorId == null || colorId.isBlank()) return 0;
        String sql = "SELECT COUNT(*) FROM ProductVariants WHERE colorId = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, colorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("countByColorId error: " + e.getMessage());
        }
        return 0;
    }

    public ProductVariant getVariantById(String variantId) {
        if (variantId == null || variantId.isBlank()) {
            return null;
        }

        String sql = "SELECT pv.variantId, pv.productId, pv.sizeId, s.sizeName, pv.colorId, c.colorName, "
                + "c.hexCode, pv.sku, pv.stockQty, pv.reservedQty, pv.priceOverride, p.name AS productName "
                + "FROM ProductVariants pv "
                + "INNER JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "INNER JOIN Colors c ON pv.colorId = c.colorId "
                + "INNER JOIN Products p ON pv.productId = p.productId "
                + "WHERE pv.variantId = ?";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                    variant.setReservedQty(rs.getInt("reservedQty"));
                    variant.setPriceOverride(rs.getBigDecimal("priceOverride"));
                    return variant;
                }
            }
        } catch (SQLException e) {
            System.out.println("getVariantById error: " + e.getMessage());
        }

        return null;
    }

    private String generateVariantId() {
        return "VAR" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
