package Controllers;

import Models.Payment;
import Models.Wallet;
import Services.PaymentService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CustomerWalletServlet", urlPatterns = {"/customer/wallet"})
public class CustomerWalletServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/Pages/Authentication/login.jsp");
            return;
        }

        Wallet wallet = paymentService.getOrCreateWallet(customerId);
        List<Payment> paymentHistory = paymentService.getPaymentHistory(customerId);

        request.setAttribute("wallet", wallet);
        request.setAttribute("paymentHistory", paymentHistory);
        request.setAttribute("contentPage", "/Pages/Customer/wallet.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }
}
