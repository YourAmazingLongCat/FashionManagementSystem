package DALs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Models.Category;
import Utils.DBContext;

/**
 * DAO for Categories used in Product Management screens.
 */
public class CategoryDAO extends DBContext {

    public CategoryDAO() {
        super();
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT categoryId, name FROM Categories ORDER BY name";

        if (connection == null) {
            System.out.println("getAllCategories error: database connection is not available.");
            return categories;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllCategories error: " + e.getMessage());
        }

        return categories;
    }

    public Category getCategoryById(String categoryId) {
        String sql = "SELECT categoryId, name FROM Categories WHERE categoryId = ?";

        if (connection == null) {
            System.out.println("getCategoryById error: database connection is not available.");
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getCategoryById error: " + e.getMessage());
        }

        return null;
    }

    public boolean createCategory(Category category) throws SQLException {
        String sql = "INSERT INTO Categories (categoryId, name) VALUES (?, ?)";

        if (connection == null) {
            throw new SQLException("Database connection is not available.");
        }

        category.setCategoryId(generateNextCategoryId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryId());
            ps.setString(2, category.getName());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateCategory(Category category) throws SQLException {
        String sql = "UPDATE Categories SET name = ? WHERE categoryId = ?";

        if (connection == null) {
            throw new SQLException("Database connection is not available.");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getCategoryId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCategory(String categoryId) throws SQLException {
        String sql = "DELETE FROM Categories WHERE categoryId = ?";

        if (connection == null) {
            throw new SQLException("Database connection is not available.");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            return ps.executeUpdate() > 0;
        }
    }

    private String generateNextCategoryId() {
        String sql = "SELECT TOP 1 categoryId FROM Categories ORDER BY categoryId DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String currentId = rs.getString("categoryId");
                if (currentId != null && currentId.length() > 3) {
                    int nextNumber = Integer.parseInt(currentId.substring(3)) + 1;
                    return String.format("CAT%03d", nextNumber);
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("generateNextCategoryId error: " + e.getMessage());
        }

        return "CAT001";
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getString("categoryId"));
        category.setName(rs.getString("name"));
        return category;
    }
}
