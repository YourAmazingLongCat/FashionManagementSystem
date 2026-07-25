package Controllers;

import Models.Account;
import Services.PaymentService;
import java.io.IOException;
import java.math.BigDecimal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WalletBalanceServlet", urlPatterns = {"/customer/wallet/balance"})
public class WalletBalanceServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String accountId = getCustomerId(session);

        if (accountId == null) {
            response.getWriter().write("{\"balance\": 0}");
            return;
        }

        var wallet = paymentService.getWalletByAccountId(accountId);
        BigDecimal balance = (wallet != null && wallet.getBalance() != null)
                ? wallet.getBalance() : BigDecimal.ZERO;

        response.getWriter().write("{\"balance\": " + balance + "}");
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
}
