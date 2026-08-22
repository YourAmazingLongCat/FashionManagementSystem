package Services;

import DALs.BillIntegrationDAO;
import DALs.OrderDAO;
import Models.Order;
import Models.Payment;
import Utils.PaymentType;
import java.math.BigDecimal;
import java.util.Random;

/**
 * Keeps bill-style payment fields on the Orders row synchronized with the
 * current order payment.
 */
public class BillIntegrationService {

    private final BillIntegrationDAO billIntegrationDAO;
    private final OrderDAO orderDAO;

    public BillIntegrationService() {
        billIntegrationDAO = new BillIntegrationDAO();
        orderDAO = new OrderDAO();
    }

    public boolean syncBillFromPayment(Payment payment) {
        if (payment == null || isEmpty(payment.getOrderId())) {
            return true;
        }

        if (!PaymentType.PURCHASE.equals(payment.getPaymentType())) {
            return true;
        }

        return syncBillForOrder(
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount()
        );
    }

    public boolean syncBillForOrder(String orderId, String paymentMethod,
            String paymentStatus, BigDecimal totalAmount) {

        if (isEmpty(orderId) || isEmpty(paymentMethod) || isEmpty(paymentStatus)) {
            return false;
        }

        BigDecimal billTotal = totalAmount;
        if (billTotal == null) {
            Order order = orderDAO.getOrderById(orderId.trim());
            if (order != null) {
                billTotal = order.getTotalAmount();
            }
        }

        if (billTotal == null) {
            return false;
        }

        return billIntegrationDAO.createOrUpdateBill(
                generateBillId(),
                orderId.trim(),
                paymentMethod.trim(),
                paymentStatus.trim(),
                billTotal
        );
    }

    public boolean updateBillPaymentStatus(String orderId, String paymentStatus) {
        if (isEmpty(orderId) || isEmpty(paymentStatus)) {
            return false;
        }

        return billIntegrationDAO.updateBillPaymentStatus(orderId.trim(), paymentStatus.trim());
    }

    private String generateBillId() {
        int number = new Random().nextInt(900) + 100;
        return "B" + System.currentTimeMillis() + number;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
