package Controllers;

import Services.PaymentService;
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
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/Pages/Authentication/login.jsp");
            return;
        }

        BigDecimal amount = parseAmount(request.getParameter("amount"));
        String paymentMethod = request.getParameter("paymentMethod");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            session.setAttribute("errorMessage", "Deposit amount is invalid.");
            response.sendRedirect(request.getContextPath() + "/customer/wallet");
            return;
        }

        String paymentId = paymentService.depositToWallet(customerId, amount, paymentMethod);

        if (paymentId != null) {
            session.setAttribute("successMessage", "Deposit completed successfully. Payment ID: " + paymentId);
        } else {
            session.setAttribute("errorMessage", "Deposit failed. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/customer/wallet");
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
