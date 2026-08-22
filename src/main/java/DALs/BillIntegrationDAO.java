package DALs;

import Utils.DBContext;
import Utils.PaymentStatus;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Bridge DAO used by Order/Payment modules.
 *
 * In the new schema, bill-style info lives directly on the Orders row
 * (paymentMethod, paymentStatus, paidAmount, issuedDate). This DAO simply
 * upserts those columns.
 */
public class BillIntegrationDAO extends DBContext {

    public boolean createOrUpdateBill(String billId, String orderId, String paymentMethod,
            String paymentStatus, BigDecimal totalAmount) {

        if (isEmpty(orderId) || isEmpty(paymentMethod)
                || isEmpty(paymentStatus) || totalAmount == null) {
            return false;
        }

        String sql = "UPDATE Orders "
                   + "SET paymentMethod = ?, paymentStatus = ?, paidAmount = ?, "
                   + "    issuedDate = COALESCE(issuedDate, GETDATE()) "
                   + "WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentMethod.trim());
            ps.setString(2, paymentStatus.trim());
            ps.setBigDecimal(3, PaymentStatus.isPaid(paymentStatus)
                    ? totalAmount : BigDecimal.ZERO);
            ps.setString(4, orderId.trim());
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

        String sql = "UPDATE Orders SET paymentStatus = ? WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentStatus.trim());
            ps.setString(2, orderId.trim());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("updateBillPaymentStatus error: " + e.getMessage());
        }

        return false;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}