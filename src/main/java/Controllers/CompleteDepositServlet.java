package Controllers;

import Services.PaymentService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CompleteDepositServlet", urlPatterns = {"/staff/complete-deposit"})
public class CompleteDepositServlet extends HttpServlet {

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
        String paymentId = request.getParameter("paymentId");
        String backUrl = request.getParameter("backUrl");

        if (paymentId == null || paymentId.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Payment ID is missing.");
        } else if (paymentService.completeDeposit(paymentId.trim())) {
            session.setAttribute("successMessage", "Deposit payment has been completed.");
        } else {
            session.setAttribute("errorMessage", "Cannot complete this deposit payment.");
        }

        if (backUrl == null || backUrl.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/staff/orders");
        } else {
            response.sendRedirect(backUrl);
        }
    }
}
