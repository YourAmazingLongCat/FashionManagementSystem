package DALs;

import Models.Category;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Categories used in Product Management screens.
 */
public class CategoryDAO extends DBContext {

    public CategoryDAO() {
        super();
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT categoryId, name, description FROM Categories ORDER BY name";

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
        String sql = "SELECT categoryId, name, description FROM Categories WHERE categoryId = ?";

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

    public boolean createCategory(Category category) {
        String sql = "INSERT INTO Categories (categoryId, name, description) VALUES (?, ?, ?)";

        if (connection == null) {
            System.out.println("createCategory error: database connection is not available.");
            return false;
        }

        category.setCategoryId(generateNextCategoryId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryId());
            ps.setString(2, category.getName());
            ps.setString(3, category.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("createCategory error: " + e.getMessage());
        }

        return false;
    }

    public boolean updateCategory(Category category) {
        String sql = "UPDATE Categories SET name = ?, description = ? WHERE categoryId = ?";

        if (connection == null) {
            System.out.println("updateCategory error: database connection is not available.");
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateCategory error: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteCategory(String categoryId) {
        String sql = "DELETE FROM Categories WHERE categoryId = ?";

        if (connection == null) {
            System.out.println("deleteCategory error: database connection is not available.");
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteCategory error: " + e.getMessage());
        }

        return false;
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
        category.setDescription(rs.getString("description"));
        return category;
    }
}
