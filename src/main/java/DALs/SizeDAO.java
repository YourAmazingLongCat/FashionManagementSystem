package DALs;

import Models.Size;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SizeDAO extends DBContext {

    public SizeDAO() {
        super();
    }

    public List<Size> getAllSizes() {
        List<Size> sizes = new ArrayList<>();
        String sql = "SELECT s.sizeId, s.sizeName, s.categoryId, c.name AS categoryName FROM Sizes s INNER JOIN Categories c ON s.categoryId = c.categoryId ORDER BY c.name, s.sizeName";

        if (connection == null) {
            return sizes;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Size size = new Size(
                        rs.getString("sizeId"),
                        rs.getString("sizeName"),
                        rs.getString("categoryId")
                );
                size.setCategoryName(rs.getString("categoryName"));
                sizes.add(size);
            }
        } catch (SQLException e) {
            System.out.println("getAllSizes error: " + e.getMessage());
        }

        return sizes;
    }

    public List<Size> getSizesByCategoryId(String categoryId) {
        List<Size> sizes = new ArrayList<>();
        String sql = "SELECT sizeId, sizeName, categoryId FROM Sizes WHERE categoryId = ? ORDER BY sizeName";

        if (connection == null || categoryId == null || categoryId.isBlank()) {
            return sizes;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sizes.add(new Size(
                            rs.getString("sizeId"),
                            rs.getString("sizeName"),
                            rs.getString("categoryId")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("getSizesByCategoryId error: " + e.getMessage());
        }

        return sizes;
    }

    public Size getSizeById(String sizeId) {
        String sql = "SELECT s.sizeId, s.sizeName, s.categoryId, c.name AS categoryName FROM Sizes s INNER JOIN Categories c ON s.categoryId = c.categoryId WHERE s.sizeId = ?";

        if (connection == null) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sizeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Size size = new Size(
                            rs.getString("sizeId"),
                            rs.getString("sizeName"),
                            rs.getString("categoryId")
                    );
                    size.setCategoryName(rs.getString("categoryName"));
                    return size;
                }
            }
        } catch (SQLException e) {
            System.out.println("getSizeById error: " + e.getMessage());
        }

        return null;
    }

    public boolean createSize(Size size) {
        String sql = "INSERT INTO Sizes (sizeId, sizeName, categoryId) VALUES (?, ?, ?)";

        if (connection == null) {
            return false;
        }

        size.setSizeId(generateNextSizeId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, size.getSizeId());
            ps.setString(2, size.getSizeName());
            ps.setString(3, size.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("createSize error: " + e.getMessage());
        }

        return false;
    }

    public boolean updateSize(Size size) {
        String sql = "UPDATE Sizes SET sizeName = ?, categoryId = ? WHERE sizeId = ?";

        if (connection == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, size.getSizeName());
            ps.setString(2, size.getCategoryId());
            ps.setString(3, size.getSizeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateSize error: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteSize(String sizeId) {
        String sql = "DELETE FROM Sizes WHERE sizeId = ?";

        if (connection == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sizeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteSize error: " + e.getMessage());
        }

        return false;
    }

    private String generateNextSizeId() {
        String sql = "SELECT TOP 1 sizeId FROM Sizes WHERE sizeId LIKE 'SIZ%' ORDER BY sizeId DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String currentId = rs.getString("sizeId");
                if (currentId != null && currentId.length() > 3) {
                    int nextNumber = Integer.parseInt(currentId.substring(3)) + 1;
                    return String.format("SIZ%03d", nextNumber);
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("generateNextSizeId error: " + e.getMessage());
        }

        return "SIZ001";
    }
}
