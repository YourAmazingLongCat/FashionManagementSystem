package DALs;

import Models.Color;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ColorDAO extends DBContext {

    public ColorDAO() {
        super();
    }

    public List<Color> getAllColors() {
        List<Color> colors = new ArrayList<>();
        if (connection == null) {
            return colors;
        }

        String sql = "SELECT colorId, colorName, hexCode FROM Colors ORDER BY colorName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                colors.add(mapColor(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllColors error: " + e.getMessage());
        }

        return colors;
    }

    public Color getColorById(String colorId) {
        String sql = "SELECT colorId, colorName, hexCode FROM Colors WHERE colorId = ?";

        if (connection == null) {
            System.out.println("getColorById error: database connection is not available.");
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, colorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapColor(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getColorById error: " + e.getMessage());
        }

        return null;
    }

    public boolean createColor(Color color) {
        String sql = "INSERT INTO Colors (colorId, colorName, hexCode) VALUES (?, ?, ?)";

        if (connection == null) {
            System.out.println("createColor error: database connection is not available.");
            return false;
        }

        color.setColorId(generateNextColorId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, color.getColorId());
            ps.setString(2, color.getColorName());
            ps.setString(3, color.getHexCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("createColor error: " + e.getMessage());
        }

        return false;
    }

    public boolean updateColor(Color color) {
        String sql = "UPDATE Colors SET colorName = ?, hexCode = ? WHERE colorId = ?";

        if (connection == null) {
            System.out.println("updateColor error: database connection is not available.");
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, color.getColorName());
            ps.setString(2, color.getHexCode());
            ps.setString(3, color.getColorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateColor error: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteColor(String colorId) {
        String sql = "DELETE FROM Colors WHERE colorId = ?";

        if (connection == null) {
            System.out.println("deleteColor error: database connection is not available.");
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, colorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteColor error: " + e.getMessage());
        }

        return false;
    }

    private String generateNextColorId() {
        String sql = "SELECT TOP 1 colorId FROM Colors WHERE colorId LIKE 'COL%' ORDER BY colorId DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String currentId = rs.getString("colorId");
                if (currentId != null && currentId.length() > 3) {
                    int nextNumber = Integer.parseInt(currentId.substring(3)) + 1;
                    return String.format("COL%03d", nextNumber);
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("generateNextColorId error: " + e.getMessage());
        }

        return "COL001";
    }

    private Color mapColor(ResultSet rs) throws SQLException {
        return new Color(
                rs.getString("colorId"),
                rs.getString("colorName"),
                rs.getString("hexCode")
        );
    }
}
