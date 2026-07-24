package Controllers;

import Models.Account;
import Models.Payment;
import Services.PaymentService;
import Utils.PaymentStatus;
import Utils.VNPayConfig;
import Utils.VNPayUtils;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "VNPayStartServlet", urlPatterns = {"/customer/vnpay/start"})
public class VNPayStartServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        startPayment(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        startPayment(request, response);
    }

    private void startPayment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String paymentId = trim(request.getParameter("paymentId"));
        String orderId = trim(request.getParameter("orderId"));
        Payment payment;

        if (isEmpty(paymentId) && !isEmpty(orderId)) {
            payment = paymentService.getOrCreateVNPayPaymentForOrder(customerId, orderId);
            paymentId = payment == null ? null : payment.getPaymentId();
        } else {
            payment = paymentService.getPaymentForCustomer(paymentId, customerId);
        }

        if (payment == null || isEmpty(paymentId)) {
            session.setAttribute("errorMessage",
                    "A VNPay payment request could not be created. Please try again.");
            response.sendRedirect(resolveFallbackUrl(request, null, orderId));
            return;
        }

        /* Re-check ownership after creating/reusing an order payment. */
        payment = paymentService.getPaymentForCustomer(paymentId, customerId);
        if (payment == null) {
            session.setAttribute("errorMessage", "This payment does not belong to your account.");
            response.sendRedirect(request.getContextPath() + "/customer/wallet");
            return;
        }

        if (PaymentStatus.PAID.equals(payment.getPaymentStatus())) {
            session.setAttribute("successMessage", "This VNPay payment has already been completed.");
            response.sendRedirect(resolveFallbackUrl(request, payment, orderId));
            return;
        }

        if (!PaymentStatus.PENDING.equals(payment.getPaymentStatus())) {
            session.setAttribute("errorMessage",
                    "This VNPay request is no longer active. Please create a new payment request.");
            response.sendRedirect(resolveFallbackUrl(request, payment, orderId));
            return;
        }

        VNPayConfig config = VNPayConfig.load(getServletContext());
        if (!config.isConfigured()) {
            session.setAttribute("errorMessage",
                    "VNPay Sandbox is not configured. Add vnpay.tmnCode and vnpay.hashSecret in WEB-INF/vnpay.properties.");
            response.sendRedirect(resolveFallbackUrl(request, payment, orderId));
            return;
        }

        String returnUrl = isEmpty(config.getReturnUrl())
                ? buildApplicationUrl(request, "/customer/vnpay-return")
                : config.getReturnUrl();
        String paymentUrl;

        try {
            paymentUrl = VNPayUtils.buildPaymentUrl(
                    config,
                    payment,
                    returnUrl,
                    getClientIp(request),
                    request.getParameter("locale"),
                    request.getParameter("bankCode")
            );
        } catch (RuntimeException e) {
            session.setAttribute("errorMessage",
                    "The VNPay payment link could not be generated. Please check the Sandbox configuration.");
            System.out.println("VNPayStartServlet error: " + e.getMessage());
            response.sendRedirect(resolveFallbackUrl(request, payment, orderId));
            return;
        }

        response.sendRedirect(paymentUrl);
    }

    private String resolveFallbackUrl(HttpServletRequest request,
            Payment payment, String requestedOrderId) {
        String orderId = payment == null ? requestedOrderId : payment.getOrderId();
        if (!isEmpty(orderId)) {
            return request.getContextPath() + "/customer/order-detail?orderId="
                    + urlEncode(orderId);
        }

        return request.getContextPath() + "/customer/wallet";
    }

    private String buildApplicationUrl(HttpServletRequest request, String path) {
        String forwardedProto = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
        String forwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));

        String scheme = isEmpty(forwardedProto) ? request.getScheme() : forwardedProto;
        String authority;

        if (!isEmpty(forwardedHost)) {
            authority = forwardedHost;
        } else {
            int port = request.getServerPort();
            boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443);
            authority = request.getServerName() + (defaultPort ? "" : ":" + port);
        }

        return scheme + "://" + authority + request.getContextPath() + path;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = firstHeaderValue(request.getHeader("X-Forwarded-For"));
        return isEmpty(forwardedFor) ? request.getRemoteAddr() : forwardedFor;
    }

    private String firstHeaderValue(String value) {
        if (isEmpty(value)) {
            return null;
        }
        int comma = value.indexOf(',');
        return comma < 0 ? value.trim() : value.substring(0, comma).trim();
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString();
        }

        Object user = session.getAttribute("USER");
        if (user instanceof Account) {
            return ((Account) user).getAccountId();
        }

        return null;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
