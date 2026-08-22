package Utils;

public final class PaymentStatus {

    public static final String PENDING = "Pending";
    public static final String PAID = "Paid";
    public static final String FAILED = "Failed";
    public static final String CANCELLED = "Cancelled";
    public static final String REFUNDED = "Refunded";

    private PaymentStatus() {
    }

    public static boolean isPaid(String value) {
        return value != null && PAID.equalsIgnoreCase(value.trim());
    }

    public static boolean isRefunded(String value) {
        return value != null && REFUNDED.equalsIgnoreCase(value.trim());
    }

    public static boolean isPending(String value) {
        return value != null && PENDING.equalsIgnoreCase(value.trim());
    }

    public static boolean isCancelled(String value) {
        return value != null && CANCELLED.equalsIgnoreCase(value.trim());
    }

    public static boolean isFailed(String value) {
        return value != null && FAILED.equalsIgnoreCase(value.trim());
    }

    public static boolean isFinal(String value) {
        return isPaid(value) || isRefunded(value)
                || isCancelled(value) || isFailed(value);
    }
}
