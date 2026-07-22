package DALs;

import Utils.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Small integration DAO used by Order/Payment modules.
 *
 * This class intentionally does not change the existing BillDAO/BillServlet
 * owned by the Bill module. It only creates or synchronises rows in Bills so
 * the existing Bill Management pages can read them normally.
 */
public class BillIntegrationDAO extends DBContext {

    public boolean createOrUpdateBill(String billId, String orderId, String paymentMethod,
            String paymentStatus, BigDecimal totalAmount) {

        if (isEmpty(billId) || isEmpty(orderId) || isEmpty(paymentMethod)
                || isEmpty(paymentStatus) || totalAmount == null) {
            return false;
        }

        String sql = "IF EXISTS (SELECT 1 FROM Bills WHERE orderId = ?) "
                + "BEGIN "
                + "UPDATE Bills "
                + "SET paymentMethod = ?, paymentStatus = ?, totalAmount = ? "
                + "WHERE orderId = ? "
                + "END "
                + "ELSE "
                + "BEGIN "
                + "INSERT INTO Bills (billId, orderId, paymentMethod, paymentStatus, issuedDate, totalAmount) "
                + "VALUES (?, ?, ?, ?, GETDATE(), ?) "
                + "END";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId.trim());

            ps.setString(2, paymentMethod.trim());
            ps.setString(3, paymentStatus.trim());
            ps.setBigDecimal(4, totalAmount);
            ps.setString(5, orderId.trim());

            ps.setString(6, billId.trim());
            ps.setString(7, orderId.trim());
            ps.setString(8, paymentMethod.trim());
            ps.setString(9, paymentStatus.trim());
            ps.setBigDecimal(10, totalAmount);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("createOrUpdateBill error for orderId=" + orderId + ": " + e.getMessage());
        }

        return false;
    }

    public boolean updateBillPaymentStatus(String orderId, String paymentStatus) {
        if (isEmpty(orderId) || isEmpty(paymentStatus)) {
            return false;
        }

        String sql = "UPDATE Bills SET paymentStatus = ? WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentStatus.trim());
            ps.setString(2, orderId.trim());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("updateBillPaymentStatus error for orderId=" + orderId + ": " + e.getMessage());
        }

        return false;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
