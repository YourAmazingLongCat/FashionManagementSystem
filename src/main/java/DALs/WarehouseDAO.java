package DALs;

import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO extends DBContext {

    public WarehouseDAO() {
        super();
    }

    public List<Object[]> getInventorySummary() {
        return getInventorySummary(null, null, null);
    }

    public List<Object[]> getInventorySummary(String keyword, String sizeFilter, String colorFilter) {
        List<Object[]> summary = new ArrayList<>();
        if (connection == null) return summary;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT pv.variantId, pv.productId, p.name AS productName, "
                + "pv.sizeId, s.sizeName, pv.colorId, c.colorName, pv.sku, pv.stockQty "
                + "FROM ProductVariants pv "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(pv.sku) LIKE ?) ");
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
        }
        if (sizeFilter != null && !sizeFilter.isBlank()) {
            sql.append("AND s.sizeId = ? ");
            params.add(sizeFilter);
        }
        if (colorFilter != null && !colorFilter.isBlank()) {
            sql.append("AND c.colorId = ? ");
            params.add(colorFilter);
        }

        sql.append("ORDER BY p.name, s.sizeName, c.colorName");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[9];
                    row[0] = rs.getString("variantId");
                    row[1] = rs.getString("productId");
                    row[2] = rs.getString("productName");
                    row[3] = rs.getString("sizeId");
                    row[4] = rs.getString("sizeName");
                    row[5] = rs.getString("colorId");
                    row[6] = rs.getString("colorName");
                    row[7] = rs.getString("sku");
                    row[8] = rs.getInt("stockQty");
                    summary.add(row);
                }
            }
        } catch (SQLException ex) {
            System.out.println("getInventorySummary error: " + ex.getMessage());
        }
        return summary;
    }

    public List<Object[]> getAllSizes() {
        List<Object[]> sizes = new ArrayList<>();
        if (connection == null) return sizes;

        String sql = "SELECT sizeId, sizeName FROM Sizes ORDER BY sizeName";
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

        String sql = "SELECT colorId, colorName, hexCode FROM Colors ORDER BY colorName";
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
                + "pv.sizeId, s.sizeName, pv.colorId, c.colorName, pv.stockQty "
                + "FROM ProductVariants pv "
                + "JOIN Products p ON pv.productId = p.productId "
                + "JOIN Sizes s ON pv.sizeId = s.sizeId "
                + "JOIN Colors c ON pv.colorId = c.colorId "
                + "WHERE pv.stockQty <= ? "
                + "ORDER BY pv.stockQty ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = rs.getString("variantId");
                    row[1] = rs.getString("productId");
                    row[2] = rs.getString("productName");
                    row[3] = rs.getString("sizeId");
                    row[4] = rs.getString("sizeName");
                    row[5] = rs.getString("colorId");
                    row[6] = rs.getString("colorName");
                    row[7] = rs.getInt("stockQty");
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
        
        int currentStock = getCurrentStock(variantId);
        if (currentStock < quantity) return false;
        
        int newStock = currentStock - quantity;

        String sql = "UPDATE ProductVariants SET stockQty = ? WHERE variantId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setString(2, variantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("deductStock error: " + ex.getMessage());
            return false;
        }
    }

    public boolean deductStockForOrder(String orderId) {
        if (connection == null || isBlank(orderId)) return false;

        String selectSql = "SELECT od.variantId, od.quantity FROM OrderDetails od WHERE od.orderId = ?";
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
