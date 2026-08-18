package Utils;

/**
 * Status values stored in {@code WalletTransactions.transactionStatus}.
 *
 * <p>The CHECK constraint on the column allows only:
 * {@code Pending}, {@code Completed}, {@code Failed}, {@code Cancelled}.
 *
 * <p>Note that {@code Paid} and {@code Refunded} (used for
 * {@code Orders.paymentStatus}) do NOT satisfy the constraint, so DAO code
 * must translate {@link PaymentStatus} values via {@link #fromPaymentStatus}
 * before writing to WalletTransactions.</p>
 */
public final class WalletTransactionStatus {

    public static final String PENDING = "Pending";
    public static final String COMPLETED = "Completed";
    public static final String FAILED = "Failed";
    public static final String CANCELLED = "Cancelled";

    private WalletTransactionStatus() {
    }

    /**
     * Translate a {@link PaymentStatus} value into the string that the
     * {@code WalletTransactions.transactionStatus} CHECK constraint accepts.
     *
     * <ul>
     *   <li>{@code Paid} → {@code Completed}</li>
     *   <li>{@code Refunded} → {@code Cancelled} (no separate row exists for
     *       the refund; we leave the row in a final negative state)</li>
     * </ul>
     */
    public static String fromPaymentStatus(String paymentStatus) {
        if (paymentStatus == null) {
            return PENDING;
        }
        String v = paymentStatus.trim();
        if (PaymentStatus.PAID.equalsIgnoreCase(v) || "Completed".equalsIgnoreCase(v)) {
            return COMPLETED;
        }
        if (PaymentStatus.REFUNDED.equalsIgnoreCase(v)) {
            return CANCELLED;
        }
        if (PaymentStatus.CANCELLED.equalsIgnoreCase(v) || "Cancelled".equalsIgnoreCase(v)) {
            return CANCELLED;
        }
        if (PaymentStatus.FAILED.equalsIgnoreCase(v) || "Failed".equalsIgnoreCase(v)) {
            return FAILED;
        }
        if (PaymentStatus.PENDING.equalsIgnoreCase(v) || "Pending".equalsIgnoreCase(v)) {
            return PENDING;
        }
        return v;
    }
}