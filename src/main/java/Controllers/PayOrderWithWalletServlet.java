package Controllers;

import Models.Account;
import Services.PaymentService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "PayOrderWithWalletServlet", urlPatterns = {"/customer/pay-with-wallet"})
public class PayOrderWithWalletServlet extends HttpServlet {

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

        String orderId = request.getParameter("orderId");

        if (orderId == null || orderId.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Order ID is missing.");
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        boolean paid = paymentService.payOrderByWallet(customerId, orderId.trim());

        if (paid) {
            session.setAttribute("successMessage", "Payment completed by wallet.");
        } else {
            session.setAttribute("errorMessage", "Payment failed. Please check your wallet balance or order status.");
        }

        response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + orderId.trim());
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
