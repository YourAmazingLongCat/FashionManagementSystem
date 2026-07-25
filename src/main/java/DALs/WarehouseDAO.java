package DALs;

import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public class WarehouseDAO extends DBContext {

    public WarehouseDAO() {
        super();
    }

    public List<Object[]> getInventorySummary() {
        return getInventorySummary(null, null, null, null);
    }

    public List<Object[]> getInventorySummary(String keyword, String sizeFilter, String colorFilter) {
        return getInventorySummary(keyword, sizeFilter, colorFilter, null);
    }

    public List<Object[]> getInventorySummary(String keyword, String sizeFilter, String colorFilter, String productFilter) {
        @SuppressWarnings("unchecked")
        List<Object[]> result = (List<Object[]>) getInventorySummaryPaginated(keyword, sizeFilter, colorFilter, productFilter, 1, 10).get("data");
        return result;
    }

    public Map<String, Object> getInventorySummaryPaginated(String keyword, String sizeFilter, String colorFilter, String productFilter, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        List<Object[]> summary = new ArrayList<>();
        if (connection == null) {
            result.put("data", summary);
            result.put("totalRecords", 0);
            result.put("totalPages", 0);
            return result;
        }

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ProductVariants pv "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId WHERE 1=1 ");
        StringBuilder sql = new StringBuilder(
                "SELECT pv.variantId, pv.productId, p.name AS productName, "
                + "pv.sizeId, s.sizeName, pv.colorId, c.colorName, pv.sku, "
                + "pv.stockQty, pv.reservedQty, pv.priceOverride, pv.createdAt "
                + "FROM ProductVariants pv "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE 1=1 ");

        List<Object> countParams = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String cond = "AND (LOWER(p.name) LIKE ? OR LOWER(pv.sku) LIKE ?) ";
            countSql.append(cond);
            sql.append(cond);
            String like = "%" + keyword.toLowerCase() + "%";
            countParams.add(like);
            countParams.add(like);
            params.add(like);
            params.add(like);
        }
        if (productFilter != null && !productFilter.isBlank()) {
            String cond = "AND p.productId = ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(productFilter);
            params.add(productFilter);
        }
        if (sizeFilter != null && !sizeFilter.isBlank()) {
            String cond = "AND s.sizeId = ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(sizeFilter);
            params.add(sizeFilter);
        }
        if (colorFilter != null && !colorFilter.isBlank()) {
            String cond = "AND c.colorId = ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(colorFilter);
            params.add(colorFilter);
        }

        int totalRecords = 0;
        try (PreparedStatement ps = connection.prepareStatement(countSql.toString())) {
            for (int i = 0; i < countParams.size(); i++) ps.setObject(i + 1, countParams.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalRecords = rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println("getInventorySummaryPaginated count error: " + ex.getMessage());
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        int offset = (page - 1) * pageSize;
        sql.append("ORDER BY p.name, s.sizeName, c.colorName OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ps.setInt(params.size() + 1, offset);
            ps.setInt(params.size() + 2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[12];
                    row[0] = rs.getString("variantId");
                    row[1] = rs.getString("productId");
                    row[2] = rs.getString("productName");
                    row[3] = rs.getString("sizeId");
                    row[4] = rs.getString("sizeName");
                    row[5] = rs.getString("colorId");
                    row[6] = rs.getString("colorName");
                    row[7] = rs.getString("sku");
                    row[8] = rs.getInt("stockQty");
                    row[9] = rs.getInt("reservedQty");
                    row[10] = rs.getBigDecimal("priceOverride");
                    row[11] = rs.getTimestamp("createdAt");
                    summary.add(row);
                }
            }
        } catch (SQLException ex) {
            System.out.println("getInventorySummaryPaginated error: " + ex.getMessage());
        }

        result.put("data", summary);
        result.put("totalRecords", totalRecords);
        result.put("totalPages", totalPages);
        return result;
    }

    public List<Object[]> getAllSizes() {
        List<Object[]> sizes = new ArrayList<>();
        if (connection == null) return sizes;

        String sql = "SELECT DISTINCT s.sizeId, s.sizeName FROM Sizes s "
                   + "JOIN ProductVariants pv ON s.sizeId = pv.sizeId "
                   + "ORDER BY s.sizeName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("sizeId");
                row[1] = rs.getString("sizeName");
                sizes.add(row);
            }
        } catch (SQLException ex) {
            System.out.println("getAllSizes error: " + ex.getMessage());
        }
        return sizes;
    }

    public List<Object[]> getAllColors() {
        List<Object[]> colors = new ArrayList<>();
        if (connection == null) return colors;

        String sql = "SELECT DISTINCT c.colorId, c.colorName, c.hexCode FROM Colors c "
                   + "JOIN ProductVariants pv ON c.colorId = pv.colorId "
                   + "ORDER BY c.colorName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("colorId");
                row[1] = rs.getString("colorName");
                row[2] = rs.getString("hexCode");
                colors.add(row);
            }
        } catch (SQLException ex) {
            System.out.println("getAllColors error: " + ex.getMessage());
        }
        return colors;
    }

    public List<Object[]> getLowStockItems(int threshold) {
        List<Object[]> items = new ArrayList<>();
        if (connection == null) return items;

        String sql = "SELECT pv.variantId, pv.productId, p.name AS productName, "
                + "pv.sizeId, s.sizeName, pv.colorId, c.colorName, "
                + "pv.stockQty, pv.reservedQty, (pv.stockQty - pv.reservedQty) AS availableStock "
                + "FROM ProductVariants pv "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE (pv.stockQty - pv.reservedQty) <= ? "
                + "ORDER BY (pv.stockQty - pv.reservedQty) ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[10];
                    row[0] = rs.getString("variantId");
                    row[1] = rs.getString("productId");
                    row[2] = rs.getString("productName");
                    row[3] = rs.getString("sizeId");
                    row[4] = rs.getString("sizeName");
                    row[5] = rs.getString("colorId");
                    row[6] = rs.getString("colorName");
                    row[7] = rs.getInt("stockQty");
                    row[8] = rs.getInt("reservedQty");
                    row[9] = rs.getInt("availableStock");
                    items.add(row);
                }
            }
        } catch (SQLException ex) {
            System.out.println("getLowStockItems error: " + ex.getMessage());
        }
        return items;
    }

    public int getCurrentStock(String variantId) {
        if (connection == null || isBlank(variantId)) return 0;
        
        String sql = "SELECT stockQty FROM ProductVariants WHERE variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("stockQty");
            }
        } catch (SQLException ex) {
            System.out.println("getCurrentStock error: " + ex.getMessage());
        }
        return 0;
    }

    public int getAvailableStock(String variantId) {
        if (connection == null || isBlank(variantId)) return 0;
        
        String sql = "SELECT (stockQty - reservedQty) AS availableStock FROM ProductVariants WHERE variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("availableStock");
            }
        } catch (SQLException ex) {
            System.out.println("getAvailableStock error: " + ex.getMessage());
        }
        return 0;
    }

    public boolean importStock(String variantId, int quantity, double importPrice, String importedBy) {
        if (connection == null || isBlank(variantId) || quantity <= 0 || isBlank(importedBy)) return false;
        
        String importId = generateId("IMP");
        String insertSql = "INSERT INTO WarehouseImports (importId, variantId, quantity, importPrice, importedBy, importDate) VALUES (?, ?, ?, ?, ?, GETDATE())";
        String updateSql = "UPDATE ProductVariants SET stockQty = stockQty + ? WHERE variantId = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psInsert = connection.prepareStatement(insertSql)) {
                psInsert.setString(1, importId);
                psInsert.setString(2, variantId);
                psInsert.setInt(3, quantity);
                psInsert.setDouble(4, importPrice);
                psInsert.setString(5, importedBy);
                psInsert.executeUpdate();
            }

            try (PreparedStatement psUpdate = connection.prepareStatement(updateSql)) {
                psUpdate.setInt(1, quantity);
                psUpdate.setString(2, variantId);
                psUpdate.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            System.out.println("importStock error: " + ex.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e) {
                System.out.println("Rollback error: " + e.getMessage());
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("setAutoCommit error: " + ex.getMessage());
            }
        }
    }

    public boolean addStock(String variantId, int quantity) {
        if (connection == null || isBlank(variantId) || quantity <= 0) return false;
        
        int currentStock = getCurrentStock(variantId);
        int newStock = currentStock + quantity;

        String sql = "UPDATE ProductVariants SET stockQty = ? WHERE variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setString(2, variantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("addStock error: " + ex.getMessage());
            return false;
        }
    }

    public boolean deductStock(String variantId, int quantity) {
        if (connection == null || isBlank(variantId) || quantity <= 0) return false;
        
        int availableStock = getAvailableStock(variantId);
        if (availableStock < quantity) return false;
        
        String sql = "UPDATE ProductVariants SET stockQty = stockQty - ? WHERE variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, variantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("deductStock error: " + ex.getMessage());
            return false;
        }
    }

    public boolean deductStockForOrder(String orderId) {
        if (connection == null || isBlank(orderId)) return false;

        String selectSql = "SELECT variantId, quantity FROM OrderItems WHERE orderId = ?";
        try (PreparedStatement psSelect = connection.prepareStatement(selectSql)) {
            psSelect.setString(1, orderId);
            try (ResultSet rs = psSelect.executeQuery()) {
                while (rs.next()) {
                    String variantId = rs.getString("variantId");
                    int quantity = rs.getInt("quantity");
                    deductStock(variantId, quantity);
                }
            }
        } catch (SQLException ex) {
            System.out.println("deductStockForOrder error: " + ex.getMessage());
            return false;
        }
        return true;
    }

    public List<Object[]> getImportHistory(String variantId) {
        List<Object[]> history = new ArrayList<>();
        if (connection == null) return history;

        String sql = "SELECT wi.importId, wi.variantId, wi.quantity, wi.importPrice, "
                + "wi.importedBy, wi.importDate, a.fullName AS importerName "
                + "FROM WarehouseImports wi "
                + "JOIN Accounts a ON wi.importedBy = a.accountId "
                + "WHERE wi.variantId = ? "
                + "ORDER BY wi.importDate DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[7];
                    row[0] = rs.getString("importId");
                    row[1] = rs.getString("variantId");
                    row[2] = rs.getInt("quantity");
                    row[3] = rs.getBigDecimal("importPrice");
                    row[4] = rs.getString("importedBy");
                    row[5] = rs.getTimestamp("importDate");
                    row[6] = rs.getString("importerName");
                    history.add(row);
                }
            }
        } catch (SQLException ex) {
            System.out.println("getImportHistory error: " + ex.getMessage());
        }
        return history;
    }

    public List<Object[]> getRecentImports(int limit) {
        Map<String, Object> result = getRecentImportsPaginated(null, null, null, null, null, 1, limit);
        @SuppressWarnings("unchecked")
        List<Object[]> imports = (List<Object[]>) result.get("data");
        return imports;
    }

    public Map<String, Object> getRecentImportsPaginated(String productFilter, String importerFilter, String dateFrom, String dateTo, String search, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        List<Object[]> imports = new ArrayList<>();
        if (connection == null) {
            result.put("data", imports);
            result.put("totalRecords", 0);
            result.put("totalPages", 0);
            return result;
        }

        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM WarehouseImports wi "
            + "JOIN ProductVariants pv ON wi.variantId = pv.variantId "
            + "JOIN Products p ON pv.productId = p.productId "
            + "JOIN Accounts a ON wi.importedBy = a.accountId "
            + "WHERE 1=1 ");
        StringBuilder sql = new StringBuilder(
            "SELECT wi.importId, wi.variantId, wi.quantity, wi.importPrice, "
            + "wi.importedBy, wi.importDate, a.fullName AS importerName, "
            + "p.name AS productName, s.sizeName, c.colorName, p.productId "
            + "FROM WarehouseImports wi "
            + "JOIN Accounts a ON wi.importedBy = a.accountId "
            + "JOIN ProductVariants pv ON wi.variantId = pv.variantId "
            + "JOIN Products p ON pv.productId = p.productId "
            + "JOIN Sizes s ON pv.sizeId = s.sizeId "
            + "JOIN Colors c ON pv.colorId = c.colorId "
            + "WHERE 1=1 ");

        List<Object> countParams = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (productFilter != null && !productFilter.isBlank()) {
            String cond = " AND p.productId = ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(productFilter);
            params.add(productFilter);
        }
        if (importerFilter != null && !importerFilter.isBlank()) {
            String cond = " AND a.accountId = ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(importerFilter);
            params.add(importerFilter);
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            String cond = " AND wi.importDate >= ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(LocalDateTime.parse(dateFrom + "T00:00:00"));
            params.add(LocalDateTime.parse(dateFrom + "T00:00:00"));
        }
        if (dateTo != null && !dateTo.isBlank()) {
            String cond = " AND wi.importDate <= ? ";
            countSql.append(cond);
            sql.append(cond);
            countParams.add(LocalDateTime.parse(dateTo + "T23:59:59"));
            params.add(LocalDateTime.parse(dateTo + "T23:59:59"));
        }
        if (search != null && !search.isBlank()) {
            String cond = " AND (p.name LIKE ? OR a.fullName LIKE ?) ";
            countSql.append(cond);
            sql.append(cond);
            String like = "%" + search + "%";
            countParams.add(like);
            countParams.add(like);
            params.add(like);
            params.add(like);
        }

        int totalRecords = 0;
        try (PreparedStatement ps = connection.prepareStatement(countSql.toString())) {
            for (int i = 0; i < countParams.size(); i++) ps.setObject(i + 1, countParams.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalRecords = rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println("getRecentImportsPaginated count error: " + ex.getMessage());
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        int offset = (page - 1) * pageSize;
        sql.append(" ORDER BY wi.importDate DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ps.setInt(params.size() + 1, offset);
            ps.setInt(params.size() + 2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[11];
                    row[0] = rs.getString("importId");
                    row[1] = rs.getString("variantId");
                    row[2] = rs.getInt("quantity");
                    row[3] = rs.getBigDecimal("importPrice");
                    row[4] = rs.getString("importedBy");
                    row[5] = rs.getTimestamp("importDate");
                    row[6] = rs.getString("importerName");
                    row[7] = rs.getString("productName");
                    row[8] = rs.getString("sizeName");
                    row[9] = rs.getString("colorName");
                    row[10] = rs.getString("productId");
                    imports.add(row);
                }
            }
        } catch (SQLException ex) {
            System.out.println("getRecentImportsPaginated error: " + ex.getMessage());
        }

        result.put("data", imports);
        result.put("totalRecords", totalRecords);
        result.put("totalPages", totalPages);
        return result;
    }

    public List<Object[]> getAllImporters() {
        List<Object[]> importers = new ArrayList<>();
        if (connection == null) return importers;
        String sql = "SELECT DISTINCT a.accountId, a.fullName FROM WarehouseImports wi "
                   + "JOIN Accounts a ON wi.importedBy = a.accountId ORDER BY a.fullName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("accountId");
                row[1] = rs.getString("fullName");
                importers.add(row);
            }
        } catch (SQLException ex) {
            System.out.println("getAllImporters error: " + ex.getMessage());
        }
        return importers;
    }

    private String generateId(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
