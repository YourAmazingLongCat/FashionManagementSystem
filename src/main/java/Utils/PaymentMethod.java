package Utils;

public final class PaymentMethod {

    public static final String VNPAY = "VNPay";
    public static final String COD = "COD";

    private PaymentMethod() {
    }

    public static boolean isSupported(String value) {
        if (value == null) {
            return false;
        }
        String method = value.trim();
        return VNPAY.equalsIgnoreCase(method) || COD.equalsIgnoreCase(method);
    }
}
