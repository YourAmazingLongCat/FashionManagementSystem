package Controllers;

import Services.PaymentService;
import Utils.VNPayConfig;
import Utils.VNPayProcessResult;
import Utils.VNPayUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "VNPayIpnServlet", urlPatterns = {"/payment/vnpay-ipn"})
public class VNPayIpnServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        VNPayConfig config = VNPayConfig.load(getServletContext());
        Map<String, String> parameters = collectVNPayParameters(request);
        String secureHash = request.getParameter("vnp_SecureHash");

        if (!config.isConfigured()
                || !config.getTmnCode().equals(parameters.get("vnp_TmnCode"))
                || !VNPayUtils.validateSignature(parameters, secureHash, config.getHashSecret())) {
            writeResponse(response, "97", "Invalid signature");
            return;
        }

        String paymentId = parameters.get("vnp_TxnRef");
        BigDecimal amount = VNPayUtils.fromVNPayAmount(parameters.get("vnp_Amount"));

        VNPayProcessResult result = paymentService.processVNPayResult(
                paymentId,
                amount,
                parameters.get("vnp_ResponseCode"),
                parameters.get("vnp_TransactionStatus"),
                parameters.get("vnp_TransactionNo"),
                parameters.get("vnp_BankCode")
        );

        switch (result) {
            case PROCESSED:
                writeResponse(response, "00", "Confirm Success");
                break;
            case ALREADY_PROCESSED:
                writeResponse(response, "02", "Payment already confirmed");
                break;
            case PAYMENT_NOT_FOUND:
                writeResponse(response, "01", "Payment not found");
                break;
            case INVALID_AMOUNT:
                writeResponse(response, "04", "Invalid amount");
                break;
            default:
                writeResponse(response, "99", "Unknown error");
                break;
        }
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

    private void writeResponse(HttpServletResponse response,
            String code, String message) throws IOException {
        response.getWriter().write("{\"RspCode\":\"" + code
                + "\",\"Message\":\"" + message + "\"}");
    }
}
