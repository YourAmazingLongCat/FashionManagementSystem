package Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jakarta.servlet.ServletContext;

public final class VNPayConfig {

    public static final String DEFAULT_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String DEFAULT_VERSION = "2.1.0";
    public static final String DEFAULT_ORDER_TYPE = "other";
    public static final String DEFAULT_LOCALE = "vn";
    public static final int DEFAULT_EXPIRE_MINUTES = 15;

    private final String payUrl;
    private final String tmnCode;
    private final String hashSecret;
    private final String version;
    private final String orderType;
    private final String locale;
    private final int expireMinutes;
    private final String returnUrl;
    private final boolean allowReturnUpdate;

    private VNPayConfig(String payUrl, String tmnCode, String hashSecret,
            String version, String orderType, String locale,
            int expireMinutes, String returnUrl, boolean allowReturnUpdate) {
        this.payUrl = payUrl;
        this.tmnCode = tmnCode;
        this.hashSecret = hashSecret;
        this.version = version;
        this.orderType = orderType;
        this.locale = locale;
        this.expireMinutes = expireMinutes;
        this.returnUrl = returnUrl;
        this.allowReturnUpdate = allowReturnUpdate;
    }

    public static VNPayConfig load(ServletContext servletContext) {
        Properties properties = new Properties();

        if (servletContext != null) {
            try (InputStream inputStream
                    = servletContext.getResourceAsStream("/WEB-INF/vnpay.properties")) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            } catch (IOException e) {
                System.out.println("Cannot load /WEB-INF/vnpay.properties: " + e.getMessage());
            }
        }

        String payUrl = resolve(properties, "vnpay.payUrl", "VNPAY_PAY_URL", DEFAULT_PAY_URL);
        String tmnCode = resolve(properties, "vnpay.tmnCode", "VNPAY_TMN_CODE", "");
        String hashSecret = resolve(properties, "vnpay.hashSecret", "VNPAY_HASH_SECRET", "");
        String version = resolve(properties, "vnpay.version", "VNPAY_VERSION", DEFAULT_VERSION);
        String orderType = resolve(properties, "vnpay.orderType", "VNPAY_ORDER_TYPE", DEFAULT_ORDER_TYPE);
        String locale = resolve(properties, "vnpay.locale", "VNPAY_LOCALE", DEFAULT_LOCALE);
        String returnUrl = resolve(properties, "vnpay.returnUrl", "VNPAY_RETURN_URL", "");
        boolean allowReturnUpdate = parseBoolean(resolve(
                properties,
                "vnpay.allowReturnUpdate",
                "VNPAY_ALLOW_RETURN_UPDATE",
                "false"
        ));
        int expireMinutes = parsePositiveInt(
                resolve(properties, "vnpay.expireMinutes", "VNPAY_EXPIRE_MINUTES",
                        String.valueOf(DEFAULT_EXPIRE_MINUTES)),
                DEFAULT_EXPIRE_MINUTES
        );

        return new VNPayConfig(payUrl, tmnCode, hashSecret, version,
                orderType, locale, expireMinutes, returnUrl, allowReturnUpdate);
    }

    private static String resolve(Properties properties, String propertyName,
            String environmentName, String defaultValue) {
        String systemValue = trim(System.getProperty(propertyName));
        if (!systemValue.isEmpty()) {
            return systemValue;
        }

        String environmentValue = trim(System.getenv(environmentName));
        if (!environmentValue.isEmpty()) {
            return environmentValue;
        }

        String fileValue = trim(properties.getProperty(propertyName));
        return fileValue.isEmpty() ? defaultValue : fileValue;
    }

    private static boolean parseBoolean(String value) {
        String normalized = trim(value).toLowerCase();
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "on".equals(normalized);
    }

    private static int parsePositiveInt(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public boolean isConfigured() {
        return !isPlaceholder(tmnCode) && !isPlaceholder(hashSecret);
    }

    private boolean isPlaceholder(String value) {
        String normalized = trim(value).toUpperCase();
        return normalized.isEmpty()
                || normalized.startsWith("REPLACE_")
                || normalized.startsWith("YOUR_");
    }

    public String getPayUrl() {
        return payUrl;
    }

    public String getTmnCode() {
        return tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public String getVersion() {
        return version;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getLocale() {
        return locale;
    }

    public int getExpireMinutes() {
        return expireMinutes;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public boolean isAllowReturnUpdate() {
        return allowReturnUpdate;
    }
}
