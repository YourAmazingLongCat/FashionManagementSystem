package Controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import Models.Payment;
import Services.PaymentService;
import Utils.PaymentStatus;
import Utils.PaymentType;
import Utils.VNPayConfig;
import Utils.VNPayProcessResult;
import Utils.VNPayUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "VNPayReturnServlet", urlPatterns = {"/customer/vnpay-return"})
public class VNPayReturnServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        VNPayConfig config = VNPayConfig.load(getServletContext());
        Map<String, String> parameters = collectVNPayParameters(request);
        String secureHash = request.getParameter("vnp_SecureHash");

        if (!config.isConfigured()
                || !config.getTmnCode().equals(parameters.get("vnp_TmnCode"))
                || !VNPayUtils.validateSignature(parameters, secureHash, config.getHashSecret())) {
            session.setAttribute("errorMessage",
                    "The VNPay response could not be verified. No payment was updated.");
            response.sendRedirect(request.getContextPath() + "/customer/wallet");
            return;
        }

        String paymentId = parameters.get("vnp_TxnRef");
        BigDecimal amount = VNPayUtils.fromVNPayAmount(parameters.get("vnp_Amount"));
        String responseCode = parameters.get("vnp_ResponseCode");
        String transactionStatus = parameters.get("vnp_TransactionStatus");
        boolean gatewaySuccess = "00".equals(responseCode)
                && "00".equals(transactionStatus);

        VNPayProcessResult result;
        if (config.isAllowReturnUpdate()) {
            /*
             * Local Sandbox fallback: localhost cannot normally receive VNPay IPN.
             * The same checksum, amount and Pending-state validation is still applied.
             * Disable this option when a public HTTPS IPN URL is available.
             */
            result = paymentService.processVNPayResult(
                    paymentId,
                    amount,
                    responseCode,
                    transactionStatus,
                    parameters.get("vnp_TransactionNo"),
                    parameters.get("vnp_BankCode")
            );
        } else {
            result = inspectCurrentPayment(paymentId, amount);
        }

        Payment payment = paymentService.getPaymentById(paymentId);
        String redirectUrl = resolveRedirectUrl(request, payment);

        if ((result == VNPayProcessResult.PROCESSED
                || result == VNPayProcessResult.ALREADY_PROCESSED)
                && gatewaySuccess
                && payment != null
                && PaymentStatus.PAID.equals(payment.getPaymentStatus())) {
            if (PaymentType.DEPOSIT.equals(payment.getPaymentType())) {
                session.setAttribute("successMessage",
                        "VNPay payment completed. The money has been added to your wallet.");
            } else {
                session.setAttribute("successMessage",
                        "VNPay payment completed. Your order is waiting for confirmation.");
            }
        } else if (result == VNPayProcessResult.INVALID_AMOUNT) {
            session.setAttribute("errorMessage",
                    "The VNPay amount did not match the payment request. No money was recorded.");
        } else if (result == VNPayProcessResult.PAYMENT_NOT_FOUND) {
            session.setAttribute("errorMessage", "The VNPay payment request was not found.");
        } else if (!gatewaySuccess) {
            session.setAttribute("errorMessage",
                    "VNPay payment was not completed. You can try again.");
        } else if (gatewaySuccess && !config.isAllowReturnUpdate()) {
            session.setAttribute("successMessage",
                    "VNPay has returned the payment result. The order is waiting for server confirmation.");
        } else {
            session.setAttribute("errorMessage",
                    "The VNPay result could not be saved. Please check the payment history.");
        }

        response.sendRedirect(redirectUrl);
    }

    private VNPayProcessResult inspectCurrentPayment(String paymentId, BigDecimal amount) {
        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            return VNPayProcessResult.PAYMENT_NOT_FOUND;
        }
        if (amount == null || payment.getAmount() == null
                || payment.getAmount().compareTo(amount) != 0) {
            return VNPayProcessResult.INVALID_AMOUNT;
        }
        if (!PaymentStatus.PENDING.equals(payment.getPaymentStatus())) {
            return VNPayProcessResult.ALREADY_PROCESSED;
        }
        return VNPayProcessResult.UPDATE_FAILED;
    }

    private Map<String, String> collectVNPayParameters(HttpServletRequest request) {
        Map<String, String> parameters = new LinkedHashMap<>();
        Enumeration<String> names = request.getParameterNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("vnp_")) {
                parameters.put(name, request.getParameter(name));
            }
        }

        return parameters;
    }

    private String resolveRedirectUrl(HttpServletRequest request, Payment payment) {
        if (payment != null && !isEmpty(payment.getOrderId())) {
            return request.getContextPath() + "/customer/order-detail?orderId="
                    + urlEncode(payment.getOrderId());
        }

        return request.getContextPath() + "/customer/wallet";
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
