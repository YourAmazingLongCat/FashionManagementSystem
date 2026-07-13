package DALs;

import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProductImageDAO extends DBContext {

    public ProductImageDAO() {
        super();
    }

    public String getPrimaryImageUrl(String productId) {
        String sql = "SELECT TOP 1 imageUrl FROM ProductImages WHERE productId = ? ORDER BY isPrimary DESC, imageId ASC";
        if (connection == null || productId == null || productId.isBlank()) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("imageUrl");
                }
            }
        } catch (SQLException e) {
            System.out.println("getPrimaryImageUrl error: " + e.getMessage());
        }
        return null;
    }

    public boolean upsertPrimaryImage(String productId, String imageUrl) {
        if (connection == null || productId == null || productId.isBlank() || imageUrl == null || imageUrl.isBlank()) {
            return false;
        }

        String updatePrimaryOff = "UPDATE ProductImages SET isPrimary = 0 WHERE productId = ?";
        String updateExisting = "UPDATE ProductImages SET imageUrl = ?, isPrimary = 1 WHERE productId = ? AND isPrimary = 1";
        String insertNew = "INSERT INTO ProductImages (imageId, productId, imageUrl, isPrimary) VALUES (?, ?, ?, 1)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psOff = connection.prepareStatement(updatePrimaryOff)) {
                psOff.setString(1, productId);
                psOff.executeUpdate();
            }

            int updated;
            try (PreparedStatement psUpdate = connection.prepareStatement(updateExisting)) {
                psUpdate.setString(1, imageUrl);
                psUpdate.setString(2, productId);
                updated = psUpdate.executeUpdate();
            }

            if (updated == 0) {
                try (PreparedStatement psInsert = connection.prepareStatement(insertNew)) {
                    psInsert.setString(1, generateImageId());
                    psInsert.setString(2, productId);
                    psInsert.setString(3, imageUrl);
                    psInsert.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            System.out.println("upsertPrimaryImage error: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private String generateImageId() {
        return "IMG" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
