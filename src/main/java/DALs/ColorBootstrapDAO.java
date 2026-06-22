package DALs;

import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColorBootstrapDAO extends DBContext {

    public ColorBootstrapDAO() {
        super();
    }

    public void ensureDefaultColor(String colorId) {
        if (connection == null || colorId == null || colorId.isBlank()) {
            return;
        }

        String checkSql = "SELECT 1 FROM Colors WHERE colorId = ?";
        String insertSql = "INSERT INTO Colors (colorId, colorName, hexCode) VALUES (?, 'Default', '#000000')";

        try (PreparedStatement check = connection.prepareStatement(checkSql)) {
            check.setString(1, colorId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("ensureDefaultColor check error: " + e.getMessage());
            return;
        }

        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            insert.setString(1, colorId);
            insert.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ensureDefaultColor insert error: " + e.getMessage());
        }
    }
}
