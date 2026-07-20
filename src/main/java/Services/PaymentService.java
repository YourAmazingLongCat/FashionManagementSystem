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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaymentService {

    private final WalletDAO walletDAO;
    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;

    public PaymentService() {
        walletDAO = new WalletDAO();
        paymentDAO = new PaymentDAO();
        orderDAO = new OrderDAO();
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

    public List<Payment> getPendingDeposits() {
        return paymentDAO.getPendingDeposits();
    }

    public Payment getPaymentByOrderId(String orderId) {
        if (isEmpty(orderId)) {
            return null;
        }

        return paymentDAO.getLatestPaymentByOrderId(orderId.trim());
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
                "Deposit request to wallet. Awaiting staff confirmation.",
                now,
                null
        );

        boolean created = paymentDAO.createPayment(payment);
        return created ? payment.getPaymentId() : null;
    }

    /*
     * Kept for backward compatibility.
     * New business rule: deposit does NOT add balance immediately.
     * Staff/Admin must approve it through /staff/payments.
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

        return paymentDAO.createPayment(payment);
    }

    public boolean createVNPayPaymentForOrder(String accountId, String orderId) {
        if (isEmpty(accountId) || isEmpty(orderId)) {
            return false;
        }

        String trimmedOrderId = orderId.trim();
        String trimmedAccountId = accountId.trim();

        Order order = orderDAO.getOrderByIdAndCustomerId(trimmedOrderId, trimmedAccountId);
        if (order == null) {
            order = orderDAO.getOrderById(trimmedOrderId);
        }

        if (order == null || order.getTotalAmount() == null) {
            return false;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(trimmedOrderId);
        if (existingPayment != null) {
            return true;
        }

        Wallet wallet = getOrCreateWallet(trimmedAccountId);
        String walletId = wallet == null ? null : wallet.getWalletId();

        Payment payment = new Payment(
                generatePaymentId(),
                walletId,
                trimmedOrderId,
                PaymentType.PURCHASE,
                PaymentMethod.VNPAY,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                "VNPay payment request for order " + trimmedOrderId,
                LocalDateTime.now(),
                null
        );

        return paymentDAO.createPayment(payment);
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
            return true;
        }

        return paymentDAO.payOrderWithWallet(
                generatePaymentId(),
                accountId.trim(),
                orderId.trim(),
                order.getTotalAmount(),
                "Pay order " + orderId.trim() + " by wallet"
        );
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
         * No payment record or COD record should not block order forwarding.
         * Only Wallet and VNPay must be Paid before the order moves forward.
         */
        if (payment == null || isCashOnDeliveryMethod(payment.getPaymentMethod())) {
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
            return true;
        }

        return paymentDAO.completeCashPayment(orderId.trim());
    }

    public boolean refundWalletPaymentIfNeeded(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        return paymentDAO.refundWalletPaymentIfNeeded(orderId.trim(), generatePaymentId());
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
