package Utils;

public final class PaymentStatus {

    public static final String PENDING = "Pending";
    public static final String PAID = "Paid";
    public static final String FAILED = "Failed";
    public static final String CANCELLED = "Cancelled";
    public static final String REFUNDED = "Refunded";

    private PaymentStatus() {
    }

    /**
     * Returns true for any value that represents a successful payment.
     *
     * <p>Callers compare values pulled from various columns:
     * <ul>
     *   <li>{@code Orders.paymentStatus} stores {@code "Paid"}</li>
     *   <li>{@code WalletTransactions.transactionStatus} stores
     *       {@code "Completed"} (the CHECK constraint forbids {@code "Paid"})</li>
     * </ul>
     * Both represent the same real-world state, so accept either spelling.</p>
     */
    public static boolean isPaid(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        return PAID.equalsIgnoreCase(v) || "Completed".equalsIgnoreCase(v);
    }

    /**
     * Returns true for any value that represents a refunded payment.
     * {@code WalletTransactions.transactionStatus} stores {@code "Cancelled"}
     * for refunds (the CHECK constraint forbids {@code "Refunded"}); accept
     * both as the same logical state.
     */
    public static boolean isRefunded(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        return REFUNDED.equalsIgnoreCase(v) || "Cancelled".equalsIgnoreCase(v);
    }

    /**
     * Returns true for any value that represents a pending payment.
     */
    public static boolean isPending(String value) {
        if (value == null) {
            return false;
        }
        return PENDING.equalsIgnoreCase(value.trim());
    }

    /**
     * Returns true for any value that represents a cancelled payment.
     */
    public static boolean isCancelled(String value) {
        if (value == null) {
            return false;
        }
        return CANCELLED.equalsIgnoreCase(value.trim());
    }

    /**
     * Returns true for any value that represents a failed payment.
     */
    public static boolean isFailed(String value) {
        if (value == null) {
            return false;
        }
        return FAILED.equalsIgnoreCase(value.trim());
    }
}
