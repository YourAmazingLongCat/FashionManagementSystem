package DALs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import Controllers.ProductManagementServlet;
import Utils.DBContext;

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

    public String getImageFileNameByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        int lastSlash = imageUrl.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlash + 1);
        }
        return null;
    }

    public boolean upsertPrimaryImage(String productId, String imageUrl) {
        if (connection == null || productId == null || productId.isBlank() || imageUrl == null || imageUrl.isBlank()) {
            return false;
        }

        String getExistingSql = "SELECT imageId, imageUrl FROM ProductImages WHERE productId = ? AND isPrimary = 1";
        String deleteSql = "DELETE FROM ProductImages WHERE productId = ?";
        String insertSql = "INSERT INTO ProductImages (imageId, productId, imageUrl, isPrimary) VALUES (?, ?, ?, 1)";

        try {
            connection.setAutoCommit(false);

            String oldImageUrl = null;
            try (PreparedStatement psGet = connection.prepareStatement(getExistingSql)) {
                psGet.setString(1, productId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        oldImageUrl = rs.getString("imageUrl");
                    }
                }
            }

            // Xoa anh cu trong folder (neu co)
            if (oldImageUrl != null && !oldImageUrl.equals(imageUrl)) {
                deleteOldImageFile(oldImageUrl);
            }

            try (PreparedStatement psDelete = connection.prepareStatement(deleteSql)) {
                psDelete.setString(1, productId);
                psDelete.executeUpdate();
            }

            try (PreparedStatement psInsert = connection.prepareStatement(insertSql)) {
                psInsert.setString(1, generateImageId());
                psInsert.setString(2, productId);
                psInsert.setString(3, imageUrl);
                psInsert.executeUpdate();
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

    private void deleteOldImageFile(String imageUrl) {
        try {
            String fileName = getImageFileNameByUrl(imageUrl);
            if (fileName == null) return;
            // Prefer external upload directory used by ProductManagementServlet
            Path externalDir = ProductManagementServlet.getExternalUploadDirectory();
            Path oldFilePath = externalDir.resolve(fileName).normalize();
            if (Files.exists(oldFilePath) && Files.isRegularFile(oldFilePath)) {
                Files.delete(oldFilePath);
                System.out.println("[ProductImageDAO] Deleted old image file from external dir: " + oldFilePath.toAbsolutePath());
                return;
            }

            // Fallback: check deployment webapps path (legacy behavior)
            try {
                Path webappsDir = Paths.get(System.getProperty("catalina.base"), "webapps", "FashionManagementSystem-1.0-SNAPSHOT", "Assets", "Images", "Product");
                Path legacyPath = webappsDir.resolve(fileName).normalize();
                if (Files.exists(legacyPath) && Files.isRegularFile(legacyPath)) {
                    Files.delete(legacyPath);
                    System.out.println("[ProductImageDAO] Deleted old image file from webapps dir: " + legacyPath.toAbsolutePath());
                }
            } catch (Exception ignore) {
            }
        } catch (Exception e) {
            System.out.println("[ProductImageDAO] Failed to delete old image: " + e.getMessage());
        }
    }

    public boolean deleteImagesByProductId(String productId) {
        if (connection == null || productId == null || productId.isBlank()) {
            return false;
        }

        String sql = "DELETE FROM ProductImages WHERE productId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, productId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.out.println("deleteImagesByProductId error: " + e.getMessage());
            return false;
        }
    }

    public String getPrimaryImageByProductId(String productId) {
        return getPrimaryImageUrl(productId);
    }

    private String generateImageId() {
        return "IMG" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
