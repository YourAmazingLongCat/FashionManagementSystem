package Services;

import DALs.OrderDAO;
import DALs.PaymentDAO;
import DALs.WalletDAO;
import Models.Order;
import Models.Payment;
import Models.Wallet;
import Utils.OrderStatus;
import Utils.PaymentMethod;
import Utils.PaymentStatus;
import Utils.PaymentType;
import Utils.WalletStatus;
import Utils.VNPayProcessResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaymentService {

    private final WalletDAO walletDAO;
    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final BillIntegrationService billIntegrationService;

    public PaymentService() {
        walletDAO = new WalletDAO();
        paymentDAO = new PaymentDAO();
        orderDAO = new OrderDAO();
        billIntegrationService = new BillIntegrationService();
    }

    public Wallet getOrCreateWallet(String accountId) {
        if (isEmpty(accountId)) {
            return null;
        }

        Wallet wallet = walletDAO.getWalletByAccountId(accountId.trim());
        if (wallet != null) {
            return wallet;
        }

        LocalDateTime now = LocalDateTime.now();
        Wallet newWallet = new Wallet(
                generateWalletId(),
                accountId.trim(),
                BigDecimal.ZERO,
                WalletStatus.ACTIVE,
                now,
                now
        );

        boolean created = walletDAO.createWallet(newWallet);
        if (!created) {
            return walletDAO.getWalletByAccountId(accountId.trim());
        }

        return newWallet;
    }

    public Wallet getWalletByAccountId(String accountId) {
        if (isEmpty(accountId)) {
            return null;
        }

        return walletDAO.getWalletByAccountId(accountId.trim());
    }

    public List<Payment> getPaymentHistory(String accountId) {
        if (isEmpty(accountId)) {
            return new ArrayList<>();
        }

        return paymentDAO.getPaymentsByAccountId(accountId.trim());
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

    public List<Payment> getPendingDeposits() {
        return paymentDAO.getPendingDeposits();
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

    public Payment getPaymentForCustomer(String paymentId, String accountId) {
        if (isEmpty(paymentId) || isEmpty(accountId)) {
            return null;
        }

        return paymentDAO.getPaymentByIdAndAccountId(
                paymentId.trim(), accountId.trim());
    }

    public String createDepositPayment(String accountId, BigDecimal amount, String paymentMethod) {
        if (isEmpty(accountId) || !isValidAmount(amount)) {
            return null;
        }

        String normalizedMethod = normalizeDepositMethod(paymentMethod);

        Wallet wallet = getOrCreateWallet(accountId.trim());
        if (wallet == null || !WalletStatus.ACTIVE.equals(wallet.getWalletStatus())) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment(
                generatePaymentId(),
                wallet.getWalletId(),
                null,
                PaymentType.DEPOSIT,
                normalizedMethod,
                PaymentStatus.PENDING,
                amount,
                "VNPay Sandbox wallet deposit request.",
                now,
                null
        );

        boolean created = paymentDAO.createPayment(payment);
        return created ? payment.getPaymentId() : null;
    }

    /*
     * Kept for backward compatibility with older controller code.
     * The wallet balance is credited only after a verified VNPay result.
     */
    public String depositToWallet(String accountId, BigDecimal amount, String paymentMethod) {
        return createDepositPayment(accountId, amount, paymentMethod);
    }

    public boolean completeDeposit(String paymentId) {
        if (isEmpty(paymentId)) {
            return false;
        }

        return paymentDAO.completeDeposit(paymentId.trim());
    }

    public boolean createCashPaymentForOrder(String accountId, String orderId) {
        return createCODPaymentForOrder(accountId, orderId);
    }

    public boolean createCODPaymentForOrder(String accountId, String orderId) {
        if (isEmpty(accountId) || isEmpty(orderId)) {
            return false;
        }

        String trimmedOrderId = orderId.trim();
        String trimmedAccountId = accountId.trim();

        Order order = orderDAO.getOrderByIdAndCustomerId(trimmedOrderId, trimmedAccountId);

        /*
         * If the customer-scoped lookup fails but the order was just created,
         * fall back to orderId so COD payment record creation is not skipped.
         */
        if (order == null) {
            order = orderDAO.getOrderById(trimmedOrderId);
        }

        if (order == null || order.getTotalAmount() == null) {
            return false;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(trimmedOrderId);
        if (existingPayment != null) {
            syncBillSafely(existingPayment);
            return true;
        }

        Wallet wallet = getOrCreateWallet(trimmedAccountId);
        String walletId = wallet == null ? null : wallet.getWalletId();

        Payment payment = new Payment(
                generatePaymentId(),
                walletId,
                trimmedOrderId,
                PaymentType.PURCHASE,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                "COD payment for order " + trimmedOrderId,
                LocalDateTime.now(),
                null
        );

        boolean created = paymentDAO.createPayment(payment);
        if (created) {
            syncBillSafely(payment);
        }

        return created;
    }

    public boolean createVNPayPaymentForOrder(String accountId, String orderId) {
        return getOrCreateVNPayPaymentForOrder(accountId, orderId) != null;
    }

    public Payment getOrCreateVNPayPaymentForOrder(String accountId, String orderId) {
        if (isEmpty(accountId) || isEmpty(orderId)) {
            return null;
        }

        String trimmedOrderId = orderId.trim();
        String trimmedAccountId = accountId.trim();

        Order order = orderDAO.getOrderByIdAndCustomerId(
                trimmedOrderId, trimmedAccountId);
        if (order == null || order.getTotalAmount() == null
                || OrderStatus.CANCELLED.equals(order.getOrderStatus())
                || OrderStatus.DELIVERED.equals(order.getOrderStatus())) {
            return null;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(trimmedOrderId);
        if (existingPayment != null) {
            if (!PaymentMethod.VNPAY.equals(existingPayment.getPaymentMethod())) {
                return null;
            }

            if (PaymentStatus.PENDING.equals(existingPayment.getPaymentStatus())
                    || PaymentStatus.PAID.equals(existingPayment.getPaymentStatus())) {
                syncBillSafely(existingPayment);
                return existingPayment;
            }

            /*
             * Failed/cancelled attempts use a new paymentId because vnp_TxnRef
             * must be unique for each new VNPay request.
             */
        }

        Wallet wallet = getOrCreateWallet(trimmedAccountId);
        if (wallet == null || !WalletStatus.ACTIVE.equals(wallet.getWalletStatus())) {
            return null;
        }

        Payment payment = new Payment(
                generatePaymentId(),
                wallet.getWalletId(),
                trimmedOrderId,
                PaymentType.PURCHASE,
                PaymentMethod.VNPAY,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                "VNPay Sandbox payment request for order " + trimmedOrderId,
                LocalDateTime.now(),
                null
        );

        boolean created = paymentDAO.createPayment(payment);
        if (!created) {
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
                || (!PaymentType.PURCHASE.equals(payment.getPaymentType())
                && !PaymentType.DEPOSIT.equals(payment.getPaymentType()))) {
            return VNPayProcessResult.INVALID_PAYMENT;
        }

        if (returnedAmount == null || payment.getAmount() == null
                || payment.getAmount().compareTo(returnedAmount) != 0) {
            return VNPayProcessResult.INVALID_AMOUNT;
        }

        boolean gatewaySuccess = "00".equals(responseCode)
                && "00".equals(transactionStatus);

        if (!PaymentStatus.PENDING.equals(payment.getPaymentStatus())) {
            return VNPayProcessResult.ALREADY_PROCESSED;
        }

        String description = buildVNPayResultDescription(
                payment, responseCode, transactionStatus, transactionNo, bankCode);
        boolean updated;

        if (gatewaySuccess) {
            if (PaymentType.DEPOSIT.equals(payment.getPaymentType())) {
                updated = paymentDAO.completeDeposit(payment.getPaymentId(), description);
            } else {
                updated = paymentDAO.completeVNPayPurchase(
                        payment.getPaymentId(), payment.getAmount(), description);
                if (updated) {
                    syncBillSafely(payment.getOrderId(), PaymentMethod.VNPAY,
                            PaymentStatus.PAID, payment.getAmount());
                }
            }
        } else {
            String unsuccessfulStatus = "24".equals(responseCode)
                    ? PaymentStatus.CANCELLED : PaymentStatus.FAILED;
            updated = paymentDAO.markVNPayPaymentUnsuccessful(
                    payment.getPaymentId(), payment.getAmount(),
                    unsuccessfulStatus, description);

            if (updated && PaymentType.PURCHASE.equals(payment.getPaymentType())) {
                syncBillSafely(payment.getOrderId(), PaymentMethod.VNPAY,
                        PaymentStatus.FAILED, payment.getAmount());
            }
        }

        if (updated) {
            return VNPayProcessResult.PROCESSED;
        }

        Payment latest = paymentDAO.getPaymentById(payment.getPaymentId());
        if (latest != null && !PaymentStatus.PENDING.equals(latest.getPaymentStatus())) {
            return VNPayProcessResult.ALREADY_PROCESSED;
        }

        return VNPayProcessResult.UPDATE_FAILED;
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

    public boolean canPayOrderByWallet(String accountId, String orderId) {
        if (isEmpty(accountId) || isEmpty(orderId)) {
            return false;
        }

        Order order = orderDAO.getOrderByIdAndCustomerId(orderId.trim(), accountId.trim());
        if (order == null || order.getTotalAmount() == null) {
            return false;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(orderId.trim());
        if (existingPayment != null && PaymentStatus.PAID.equals(existingPayment.getPaymentStatus())) {
            return true;
        }

        Wallet wallet = getOrCreateWallet(accountId.trim());
        return wallet != null
                && WalletStatus.ACTIVE.equals(wallet.getWalletStatus())
                && wallet.getBalance() != null
                && wallet.getBalance().compareTo(order.getTotalAmount()) >= 0;
    }

    public boolean canPayAmountByWallet(String accountId, BigDecimal amount) {
        if (isEmpty(accountId) || !isValidAmount(amount)) {
            return false;
        }

        Wallet wallet = getOrCreateWallet(accountId.trim());
        return wallet != null
                && WalletStatus.ACTIVE.equals(wallet.getWalletStatus())
                && wallet.getBalance() != null
                && wallet.getBalance().compareTo(amount) >= 0;
    }

    public boolean payOrderByWallet(String accountId, String orderId) {
        if (isEmpty(accountId) || isEmpty(orderId)) {
            return false;
        }

        Order order = orderDAO.getOrderByIdAndCustomerId(orderId.trim(), accountId.trim());
        if (order == null || order.getTotalAmount() == null) {
            return false;
        }

        if (OrderStatus.CANCELLED.equals(order.getOrderStatus())
                || OrderStatus.DELIVERED.equals(order.getOrderStatus())) {
            return false;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(orderId.trim());
        if (existingPayment != null && PaymentStatus.PAID.equals(existingPayment.getPaymentStatus())) {
            syncBillSafely(existingPayment);
            return true;
        }

        boolean paid = paymentDAO.payOrderWithWallet(
                generatePaymentId(),
                accountId.trim(),
                orderId.trim(),
                order.getTotalAmount(),
                "Pay order " + orderId.trim() + " by wallet"
        );

        if (paid) {
            syncBillSafely(orderId.trim(), PaymentMethod.WALLET, PaymentStatus.PAID, order.getTotalAmount());
        }

        return paid;
    }

    public boolean canMoveToShippingStatus(String orderId, String newStatus) {
        /*
         * Backward compatibility for old code paths.
         * Only Wallet and VNPay require Paid status before an order moves forward.
         * COD orders can move forward without payment being Paid.
         */
        return canForwardOrderStatusByPayment(orderId);
    }

    public boolean canForwardOrderStatusByPayment(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        Payment payment = paymentDAO.getLatestPaymentByOrderId(orderId.trim());

        /*
         * A payment record is required before staff can confirm an order.
         * The Cart Checkout button creates the Pending order before the
         * customer selects a payment method, so a null payment means checkout
         * information is still incomplete. COD does not need to be Paid.
         */
        if (payment == null) {
            return false;
        }

        if (isCashOnDeliveryMethod(payment.getPaymentMethod())) {
            return true;
        }

        if (isWalletOrVNPayMethod(payment.getPaymentMethod())) {
            return PaymentStatus.PAID.equals(payment.getPaymentStatus());
        }

        return true;
    }

    public boolean completeCashPaymentForDeliveredOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        Payment payment = paymentDAO.getLatestPaymentByOrderId(orderId.trim());

        if (payment == null || !isCashOnDeliveryMethod(payment.getPaymentMethod())) {
            return true;
        }

        if (PaymentStatus.PAID.equals(payment.getPaymentStatus())) {
            syncBillSafely(payment);
            return true;
        }

        boolean completed = paymentDAO.completeCashPayment(orderId.trim());
        if (completed) {
            syncBillSafely(orderId.trim(), PaymentMethod.COD, PaymentStatus.PAID, payment.getAmount());
        }

        return completed;
    }

    public boolean refundWalletPaymentIfNeeded(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        boolean refunded = paymentDAO.refundWalletPaymentIfNeeded(orderId.trim(), generatePaymentId());

        if (refunded) {
            Payment latestPurchasePayment = paymentDAO.getLatestPaymentByOrderId(orderId.trim());
            if (latestPurchasePayment != null
                    && PaymentMethod.WALLET.equals(latestPurchasePayment.getPaymentMethod())
                    && PaymentStatus.REFUNDED.equals(latestPurchasePayment.getPaymentStatus())) {
                syncBillSafely(latestPurchasePayment);
            }
        }

        return refunded;
    }

    public boolean lockWallet(String accountId) {
        Wallet wallet = getWalletByAccountId(accountId);
        if (wallet == null) {
            return false;
        }

        return walletDAO.updateWalletStatus(wallet.getWalletId(), WalletStatus.LOCKED);
    }

    public boolean unlockWallet(String accountId) {
        Wallet wallet = getWalletByAccountId(accountId);
        if (wallet == null) {
            return false;
        }

        return walletDAO.updateWalletStatus(wallet.getWalletId(), WalletStatus.ACTIVE);
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
            billIntegrationService.syncBillForOrder(orderId, paymentMethod, paymentStatus, totalAmount);
        } catch (RuntimeException e) {
            System.out.println("syncBillForOrder warning: " + e.getMessage());
        }
    }

    private boolean isWalletOrVNPayMethod(String paymentMethod) {
        if (isEmpty(paymentMethod)) {
            return false;
        }

        String method = paymentMethod.trim();

        return PaymentMethod.WALLET.equalsIgnoreCase(method)
                || PaymentMethod.VNPAY.equalsIgnoreCase(method);
    }

    private boolean isCashOnDeliveryMethod(String paymentMethod) {
        if (isEmpty(paymentMethod)) {
            return false;
        }

        return PaymentMethod.COD.equalsIgnoreCase(paymentMethod.trim());
    }

    private String normalizeDepositMethod(String paymentMethod) {
        return PaymentMethod.VNPAY;
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private String generateWalletId() {
        Random random = new Random();
        int number = random.nextInt(900) + 100;
        return "W" + System.currentTimeMillis() + number;
    }

    private String generatePaymentId() {
        Random random = new Random();
        int number = random.nextInt(900) + 100;
        return "PM" + System.currentTimeMillis() + number;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
