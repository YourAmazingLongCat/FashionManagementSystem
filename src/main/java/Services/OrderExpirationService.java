package Services;

import DALs.OrderExpirationDAO;
import Models.ExpiredOrderInfo;
import Utils.EmailUtils;
import java.util.List;

public class OrderExpirationService {

    private final OrderExpirationDAO expirationDAO;

    public OrderExpirationService() {
        expirationDAO = new OrderExpirationDAO();
    }

    public int expirePendingOrders() {
        List<ExpiredOrderInfo> expiredOrders = expirationDAO.getExpiredPendingOrders();
        int deletedCount = 0;

        for (ExpiredOrderInfo order : expiredOrders) {
            if (!expirationDAO.expirePendingOrder(order.getOrderId())) {
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
}
