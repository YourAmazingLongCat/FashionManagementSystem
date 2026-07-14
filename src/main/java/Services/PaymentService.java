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

        Wallet wallet = walletDAO.getWalletByAccountId(accountId);
        if (wallet != null) {
            return wallet;
        }

        LocalDateTime now = LocalDateTime.now();
        Wallet newWallet = new Wallet(
                generateWalletId(),
                accountId,
                BigDecimal.ZERO,
                WalletStatus.ACTIVE,
                now,
                now
        );

        boolean created = walletDAO.createWallet(newWallet);
        if (!created) {
            return walletDAO.getWalletByAccountId(accountId);
        }

        return newWallet;
    }

    public Wallet getWalletByAccountId(String accountId) {
        if (isEmpty(accountId)) {
            return null;
        }

        return walletDAO.getWalletByAccountId(accountId);
    }

    public List<Payment> getPaymentHistory(String accountId) {
        if (isEmpty(accountId)) {
            return new ArrayList<>();
        }

        return paymentDAO.getPaymentsByAccountId(accountId);
    }

    public Payment getPaymentByOrderId(String orderId) {
        if (isEmpty(orderId)) {
            return null;
        }

        return paymentDAO.getLatestPaymentByOrderId(orderId);
    }

    public String createDepositPayment(String accountId, BigDecimal amount, String paymentMethod) {
        if (isEmpty(accountId) || !isValidAmount(amount)) {
            return null;
        }

        if (!isDepositMethod(paymentMethod)) {
            paymentMethod = PaymentMethod.BANK_TRANSFER;
        }

        Wallet wallet = getOrCreateWallet(accountId);
        if (wallet == null || !WalletStatus.ACTIVE.equals(wallet.getWalletStatus())) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment(
                generatePaymentId(),
                wallet.getWalletId(),
                null,
                PaymentType.DEPOSIT,
                paymentMethod,
                PaymentStatus.PENDING,
                amount,
                "Deposit money to wallet",
                now,
                null
        );

        boolean created = paymentDAO.createPayment(payment);
        if (!created) {
            return null;
        }

        return payment.getPaymentId();
    }

    public String depositToWallet(String accountId, BigDecimal amount, String paymentMethod) {
        String paymentId = createDepositPayment(accountId, amount, paymentMethod);

        if (paymentId == null) {
            return null;
        }

        boolean completed = paymentDAO.completeDeposit(paymentId);
        if (!completed) {
            return null;
        }

        return paymentId;
    }

    public boolean completeDeposit(String paymentId) {
        if (isEmpty(paymentId)) {
            return false;
        }

        return paymentDAO.completeDeposit(paymentId);
    }

    public boolean payOrderByWallet(String accountId, String orderId) {
        if (isEmpty(accountId) || isEmpty(orderId)) {
            return false;
        }

        Order order = orderDAO.getOrderByIdAndCustomerId(orderId, accountId);
        if (order == null || order.getTotalAmount() == null) {
            return false;
        }

        if (OrderStatus.CANCELLED.equals(order.getOrderStatus())
                || OrderStatus.DELIVERED.equals(order.getOrderStatus())) {
            return false;
        }

        Payment existingPayment = paymentDAO.getLatestPaymentByOrderId(orderId);
        if (existingPayment != null && PaymentStatus.PAID.equals(existingPayment.getPaymentStatus())) {
            return true;
        }

        Wallet wallet = getOrCreateWallet(accountId);
        if (wallet == null) {
            return false;
        }

        return paymentDAO.payOrderWithWallet(
                generatePaymentId(),
                accountId,
                orderId,
                order.getTotalAmount(),
                "Pay order " + orderId + " by wallet"
        );
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

    private boolean isDepositMethod(String paymentMethod) {
        return PaymentMethod.BANK_TRANSFER.equals(paymentMethod)
                || PaymentMethod.MOMO.equals(paymentMethod)
                || PaymentMethod.VNPAY.equals(paymentMethod);
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
