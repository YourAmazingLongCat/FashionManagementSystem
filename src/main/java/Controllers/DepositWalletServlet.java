package Controllers;

import Models.Account;
import Services.PaymentService;
import Utils.PaymentMethod;
import java.io.IOException;
import java.math.BigDecimal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DepositWalletServlet", urlPatterns = {"/customer/wallet/deposit"})
public class DepositWalletServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        BigDecimal amount = parseAmount(request.getParameter("amount"));
        String paymentMethod = normalizeDepositMethod(request.getParameter("paymentMethod"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            session.setAttribute("errorMessage", "Deposit amount is invalid.");
            response.sendRedirect(request.getContextPath() + "/customer/wallet");
            return;
        }

        String paymentId = paymentService.createDepositPayment(customerId, amount, paymentMethod);

        if (paymentId != null) {
            session.setAttribute("successMessage",
                    "Deposit request created. Please wait for staff confirmation. Payment ID: " + paymentId);
        } else {
            session.setAttribute("errorMessage", "Deposit request failed. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/customer/wallet");
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

    private String normalizeDepositMethod(String method) {
        return PaymentMethod.VNPAY;
    }

    private BigDecimal parseAmount(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
