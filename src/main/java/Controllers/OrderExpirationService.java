package Controllers;

import DALs.OrderExpirationDAO;
import Models.ExpiredOrderInfo;
import Utils.EmailUtils;
import java.util.List;
import java.util.Random;

public class OrderExpirationService {

    private final OrderExpirationDAO expirationDAO;

    public OrderExpirationService() {
        expirationDAO = new OrderExpirationDAO();
    }

    public int expirePendingOrders() {
        List<ExpiredOrderInfo> expiredOrders = expirationDAO.getExpiredPendingOrders();
        int deletedCount = 0;

        for (ExpiredOrderInfo order : expiredOrders) {
            boolean deleted = expirationDAO.expirePendingOrder(
                    order.getOrderId(), generateRefundPaymentId());

            if (!deleted) {
                continue;
            }

            deletedCount++;

            if (order.getCustomerEmail() != null
                    && !order.getCustomerEmail().trim().isEmpty()) {
                EmailUtils.sendOrderExpiredNotification(
                        order.getCustomerEmail(),
                        order.getCustomerName(),
                        order.getOrderId(),
                        order.getPlacedAt(),
                        order.getTotalAmount()
                );
            }
        }

        return deletedCount;
    }

    private String generateRefundPaymentId() {
        int number = new Random().nextInt(900) + 100;
        return "RF" + System.currentTimeMillis() + number;
    }
}
