package Services;

import DALs.OrderDAO;
import DALs.PaymentDAO;
import Models.Order;
import Models.Payment;
import Utils.OrderStatus;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import Utils.PaymentType;
import Utils.VNPayProcessResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Business rules for COD and VNPay payments.
 */
public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final BillIntegrationService billIntegrationService;

    public PaymentService() {
        paymentDAO = new PaymentDAO();
        orderDAO = new OrderDAO();
        billIntegrationService = new BillIntegrationService();
    }

    public List<Payment> getPaymentHistory(String customerId) {
        if (isEmpty(customerId)) {
            return new ArrayList<>();
        }
        return paymentDAO.getPaymentsByAccountId(customerId.trim());
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.getAllPayments();
    }

    public int countAllPayments() {
        return paymentDAO.countAllPayments();
    }

    public List<Payment> getAllPaymentsPaginated(int offset, int limit) {
        return paymentDAO.getAllPaymentsPaginated(offset, limit);
    }

    public Payment getPaymentByOrderId(String orderId) {
        if (isEmpty(orderId)) {
            return null;
        }
        return paymentDAO.getLatestPaymentByOrderId(orderId.trim());
    }

    public Payment getPaymentById(String paymentId) {
        if (isEmpty(paymentId)) {
            return null;
        }
        return paymentDAO.getPaymentById(paymentId.trim());
    }

    public Payment getPaymentForCustomer(String paymentId, String customerId) {
        if (isEmpty(paymentId) || isEmpty(customerId)) {
            return null;
        }
        return paymentDAO.getPaymentByIdAndAccountId(
                paymentId.trim(), customerId.trim());
    }

    public boolean createCashPaymentForOrder(String customerId, String orderId) {
        return createCODPaymentForOrder(customerId, orderId);
    }

    public boolean createCODPaymentForOrder(String customerId, String orderId) {
        if (isEmpty(customerId) || isEmpty(orderId)) {
            return false;
        }

        String normalizedCustomerId = customerId.trim();
        String normalizedOrderId = orderId.trim();
        Order order = orderDAO.getOrderByIdAndCustomerId(
                normalizedOrderId, normalizedCustomerId);

        if (!canCreatePaymentForOrder(order)) {
            return false;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(normalizedOrderId);
        if (existingPayment != null
                && (PaymentStatus.isPending(existingPayment.getPaymentStatus())
                || PaymentStatus.isPaid(existingPayment.getPaymentStatus()))) {
            return PaymentMethod.COD.equals(existingPayment.getPaymentMethod());
        }

        Payment payment = new Payment(
                generatePaymentId(),
                normalizedOrderId,
                PaymentType.PURCHASE,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                "COD payment for order " + normalizedOrderId,
                LocalDateTime.now(),
                null
        );

        boolean created = paymentDAO.createOrderPayment(payment, normalizedCustomerId);
        if (created) {
            syncBillSafely(payment);
        }
        return created;
    }

    public boolean createVNPayPaymentForOrder(String customerId, String orderId) {
        return getOrCreateVNPayPaymentForOrder(customerId, orderId) != null;
    }

    public Payment getOrCreateVNPayPaymentForOrder(String customerId, String orderId) {
        if (isEmpty(customerId) || isEmpty(orderId)) {
            return null;
        }

        String normalizedCustomerId = customerId.trim();
        String normalizedOrderId = orderId.trim();
        Order order = orderDAO.getOrderByIdAndCustomerId(
                normalizedOrderId, normalizedCustomerId);

        if (!canCreatePaymentForOrder(order)) {
            return null;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(normalizedOrderId);
        if (existingPayment != null) {
            if (!PaymentMethod.VNPAY.equals(existingPayment.getPaymentMethod())
                    && (PaymentStatus.isPending(existingPayment.getPaymentStatus())
                    || PaymentStatus.isPaid(existingPayment.getPaymentStatus()))) {
                return null;
            }

            if (PaymentMethod.VNPAY.equals(existingPayment.getPaymentMethod())
                    && (PaymentStatus.isPending(existingPayment.getPaymentStatus())
                    || PaymentStatus.isPaid(existingPayment.getPaymentStatus()))) {
                syncBillSafely(existingPayment);
                return existingPayment;
            }
        }

        Payment payment = new Payment(
                generatePaymentId(),
                normalizedOrderId,
                PaymentType.PURCHASE,
                PaymentMethod.VNPAY,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                "VNPay Sandbox payment request for order " + normalizedOrderId,
                LocalDateTime.now(),
                null
        );

        if (!paymentDAO.createOrderPayment(payment, normalizedCustomerId)) {
            return null;
        }

        syncBillSafely(payment);
        return payment;
    }

    public VNPayProcessResult processVNPayResult(String paymentId,
            BigDecimal returnedAmount, String responseCode,
            String transactionStatus, String transactionNo, String bankCode) {
        if (isEmpty(paymentId)) {
            return VNPayProcessResult.PAYMENT_NOT_FOUND;
        }

        Payment payment = paymentDAO.getPaymentById(paymentId.trim());
        if (payment == null) {
            return VNPayProcessResult.PAYMENT_NOT_FOUND;
        }

        if (!PaymentMethod.VNPAY.equals(payment.getPaymentMethod())
                || !PaymentType.PURCHASE.equals(payment.getPaymentType())) {
            return VNPayProcessResult.INVALID_PAYMENT;
        }

        if (returnedAmount == null || payment.getAmount() == null
                || payment.getAmount().compareTo(returnedAmount) != 0) {
            return VNPayProcessResult.INVALID_AMOUNT;
        }

        if (!PaymentStatus.isPending(payment.getPaymentStatus())) {
            return VNPayProcessResult.ALREADY_PROCESSED;
        }

        boolean gatewaySuccess = "00".equals(responseCode)
                && "00".equals(transactionStatus);
        String description = buildVNPayResultDescription(
                payment, responseCode, transactionStatus, transactionNo, bankCode);
        boolean updated;

        if (gatewaySuccess) {
            updated = paymentDAO.completeVNPayPurchase(
                    payment.getPaymentId(), payment.getAmount(), description);
            if (updated) {
                syncBillSafely(payment.getOrderId(), PaymentMethod.VNPAY,
                        PaymentStatus.PAID, payment.getAmount());
            }
        } else {
            String unsuccessfulStatus = "24".equals(responseCode)
                    ? PaymentStatus.CANCELLED : PaymentStatus.FAILED;
            updated = paymentDAO.markVNPayPaymentUnsuccessful(
                    payment.getPaymentId(), payment.getAmount(),
                    unsuccessfulStatus, description);
            if (updated) {
                syncBillSafely(payment.getOrderId(), PaymentMethod.VNPAY,
                        unsuccessfulStatus, payment.getAmount());
            }
        }

        if (updated) {
            return VNPayProcessResult.PROCESSED;
        }

        Payment latest = paymentDAO.getPaymentById(payment.getPaymentId());
        if (latest != null && !PaymentStatus.isPending(latest.getPaymentStatus())) {
            return VNPayProcessResult.ALREADY_PROCESSED;
        }
        return VNPayProcessResult.UPDATE_FAILED;
    }

    public boolean canMoveToShippingStatus(String orderId, String newStatus) {
        return canForwardOrderStatusByPayment(orderId);
    }

    public boolean canForwardOrderStatusByPayment(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment == null) {
            return false;
        }

        if (PaymentMethod.COD.equals(payment.getPaymentMethod())) {
            return PaymentStatus.isPending(payment.getPaymentStatus())
                    || PaymentStatus.isPaid(payment.getPaymentStatus());
        }

        if (PaymentMethod.VNPAY.equals(payment.getPaymentMethod())) {
            return PaymentStatus.isPaid(payment.getPaymentStatus());
        }

        return false;
    }

    public boolean completeCashPaymentForDeliveredOrder(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment == null || !PaymentMethod.COD.equals(payment.getPaymentMethod())) {
            return true;
        }

        if (PaymentStatus.isPaid(payment.getPaymentStatus())) {
            syncBillSafely(payment);
            return true;
        }

        boolean completed = paymentDAO.completeCashPayment(orderId.trim());
        if (completed) {
            syncBillSafely(orderId.trim(), PaymentMethod.COD,
                    PaymentStatus.PAID, payment.getAmount());
        }
        return completed;
    }

    public boolean cancelOrderPaymentIfNeeded(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment == null) {
            return true;
        }

        if (PaymentStatus.isRefunded(payment.getPaymentStatus())
                || PaymentStatus.isCancelled(payment.getPaymentStatus())
                || PaymentStatus.isFailed(payment.getPaymentStatus())) {
            syncBillSafely(payment);
            return true;
        }

        if (PaymentMethod.VNPAY.equals(payment.getPaymentMethod())) {
            if (PaymentStatus.isPaid(payment.getPaymentStatus())) {
                return refundVNPayPaymentIfNeeded(orderId);
            }
            if (PaymentStatus.isPending(payment.getPaymentStatus())) {
                boolean cancelled = paymentDAO.markVNPayPaymentUnsuccessful(
                        payment.getPaymentId(), payment.getAmount(),
                        PaymentStatus.CANCELLED,
                        "VNPay payment cancelled because order "
                        + orderId.trim() + " was cancelled");
                if (cancelled) {
                    syncBillSafely(orderId.trim(), PaymentMethod.VNPAY,
                            PaymentStatus.CANCELLED, payment.getAmount());
                }
                return cancelled;
            }
        }

        if (PaymentMethod.COD.equals(payment.getPaymentMethod())) {
            if (PaymentStatus.isPending(payment.getPaymentStatus())) {
                boolean cancelled = paymentDAO.cancelCashPayment(orderId.trim());
                if (cancelled) {
                    syncBillSafely(orderId.trim(), PaymentMethod.COD,
                            PaymentStatus.CANCELLED, payment.getAmount());
                }
                return cancelled;
            }
            return PaymentStatus.isPaid(payment.getPaymentStatus());
        }

        return false;
    }

    public boolean cancelCashPaymentIfNeeded(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment == null || !PaymentMethod.COD.equals(payment.getPaymentMethod())) {
            return true;
        }
        if (PaymentStatus.isCancelled(payment.getPaymentStatus())) {
            return true;
        }
        return paymentDAO.cancelCashPayment(orderId.trim());
    }

    public boolean refundVNPayPaymentIfNeeded(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        if (payment == null || !PaymentMethod.VNPAY.equals(payment.getPaymentMethod())) {
            return true;
        }
        if (PaymentStatus.isRefunded(payment.getPaymentStatus())) {
            return true;
        }
        if (!PaymentStatus.isPaid(payment.getPaymentStatus())) {
            return false;
        }

        boolean refunded = paymentDAO.refundVNPayPaymentByOrderId(
                orderId.trim(),
                "VNPay refund recorded for cancelled order " + orderId.trim());
        if (refunded) {
            syncBillSafely(orderId.trim(), PaymentMethod.VNPAY,
                    PaymentStatus.REFUNDED, payment.getAmount());
        }
        return refunded;
    }

    public boolean refundPaymentIfNeeded(String orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        return payment != null
                && PaymentMethod.VNPAY.equals(payment.getPaymentMethod())
                && PaymentStatus.isPaid(payment.getPaymentStatus())
                && refundVNPayPaymentIfNeeded(orderId);
    }

    private boolean canCreatePaymentForOrder(Order order) {
        return order != null
                && order.getTotalAmount() != null
                && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                && !OrderStatus.CANCELLED.equals(order.getOrderStatus())
                && !OrderStatus.DELIVERED.equals(order.getOrderStatus());
    }

    private String buildVNPayResultDescription(Payment payment,
            String responseCode, String transactionStatus,
            String transactionNo, String bankCode) {
        StringBuilder description = new StringBuilder();
        description.append("VNPay Sandbox result for payment ")
                .append(payment.getPaymentId());

        if (!isEmpty(transactionNo)) {
            description.append("; transactionNo=").append(transactionNo.trim());
        }
        if (!isEmpty(bankCode)) {
            description.append("; bankCode=").append(bankCode.trim());
        }
        if (!isEmpty(responseCode)) {
            description.append("; responseCode=").append(responseCode.trim());
        }
        if (!isEmpty(transactionStatus)) {
            description.append("; transactionStatus=")
                    .append(transactionStatus.trim());
        }
        return description.toString();
    }

    private void syncBillSafely(Payment payment) {
        try {
            billIntegrationService.syncBillFromPayment(payment);
        } catch (RuntimeException e) {
            System.out.println("syncBillFromPayment warning: " + e.getMessage());
        }
    }

    private void syncBillSafely(String orderId, String paymentMethod,
            String paymentStatus, BigDecimal totalAmount) {
        try {
            billIntegrationService.syncBillForOrder(
                    orderId, paymentMethod, paymentStatus, totalAmount);
        } catch (RuntimeException e) {
            System.out.println("syncBillForOrder warning: " + e.getMessage());
        }
    }

    private String generatePaymentId() {
        int number = new Random().nextInt(900) + 100;
        return "PM" + System.currentTimeMillis() + number;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
