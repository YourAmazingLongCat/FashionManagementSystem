package Utils;

import Models.Payment;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class VNPayUtils {

    private static final BigDecimal VNPAY_AMOUNT_MULTIPLIER = new BigDecimal("100");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private VNPayUtils() {
    }

    public static String buildPaymentUrl(VNPayConfig config, Payment payment,
            String returnUrl, String ipAddress, String locale, String bankCode) {
        if (config == null || payment == null || payment.getAmount() == null) {
            throw new IllegalArgumentException("VNPay configuration and payment are required.");
        }

        LocalDateTime createTime = LocalDateTime.now(VIETNAM_ZONE);
        LocalDateTime expireTime = createTime.plusMinutes(config.getExpireMinutes());

        Map<String, String> parameters = new TreeMap<>();
        parameters.put("vnp_Version", config.getVersion());
        parameters.put("vnp_Command", "pay");
        parameters.put("vnp_TmnCode", config.getTmnCode());
        parameters.put("vnp_Amount", toVNPayAmount(payment.getAmount()));
        parameters.put("vnp_CurrCode", "VND");
        parameters.put("vnp_TxnRef", payment.getPaymentId());
        parameters.put("vnp_OrderInfo", buildOrderInfo(payment));
        parameters.put("vnp_OrderType", config.getOrderType());
        parameters.put("vnp_Locale", isBlank(locale) ? config.getLocale() : locale.trim());
        parameters.put("vnp_ReturnUrl", returnUrl);
        parameters.put("vnp_IpAddr", isBlank(ipAddress) ? "127.0.0.1" : ipAddress.trim());
        parameters.put("vnp_CreateDate", createTime.format(VNPAY_DATE_FORMAT));
        parameters.put("vnp_ExpireDate", expireTime.format(VNPAY_DATE_FORMAT));

        if (!isBlank(bankCode)) {
            parameters.put("vnp_BankCode", bankCode.trim());
        }

        String query = buildEncodedQuery(parameters);
        String secureHash = hmacSHA512(config.getHashSecret(), query);
        return config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public static boolean validateSignature(Map<String, String> responseParameters,
            String receivedSecureHash, String hashSecret) {
        if (responseParameters == null || isBlank(receivedSecureHash)
                || isBlank(hashSecret)) {
            return false;
        }

        Map<String, String> signedFields = new TreeMap<>();
        for (Map.Entry<String, String> entry : responseParameters.entrySet()) {
            String key = entry.getKey();
            if (key == null
                    || "vnp_SecureHash".equals(key)
                    || "vnp_SecureHashType".equals(key)) {
                continue;
            }

            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                signedFields.put(key, value);
            }
        }

        String calculatedHash = hmacSHA512(hashSecret, buildEncodedQuery(signedFields));
        return MessageDigest.isEqual(
                calculatedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII),
                receivedSecureHash.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII)
        );
    }

    public static BigDecimal fromVNPayAmount(String rawAmount) {
        if (isBlank(rawAmount)) {
            return null;
        }

        try {
            return new BigDecimal(rawAmount.trim()).movePointLeft(2);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String buildEncodedQuery(Map<String, String> parameters) {
        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : new TreeMap<>(parameters).entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (!first) {
                query.append('&');
            }

            query.append(urlEncode(entry.getKey()));
            query.append('=');
            query.append(urlEncode(value));
            first = false;
        }

        return query.toString();
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Cannot create VNPay secure hash.", e);
        }
    }

    private static String toVNPayAmount(BigDecimal amount) {
        return amount.multiply(VNPAY_AMOUNT_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .toBigIntegerExact()
                .toString();
    }

    private static String buildOrderInfo(Payment payment) {
        if (PaymentType.DEPOSIT.equals(payment.getPaymentType())) {
            return "Nap tien vao vi " + payment.getPaymentId();
        }

        if (!isBlank(payment.getOrderId())) {
            return "Thanh toan don hang " + payment.getOrderId();
        }

        return "Thanh toan giao dich " + payment.getPaymentId();
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.US_ASCII.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("US-ASCII is not supported.", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
