package Controllers;

import Models.Account;
import Models.Payment;
import Services.PaymentService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StaffPaymentServlet", urlPatterns = {"/staff/payments"})
public class StaffPaymentServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account user = (Account) session.getAttribute("USER");

        if (user == null || !isStaffOrAdmin(user)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<Payment> payments = paymentService.getAllPayments();
        List<Payment> pendingDeposits = paymentService.getPendingDeposits();

        request.setAttribute("payments", payments);
        request.setAttribute("pendingDeposits", pendingDeposits);
        request.setAttribute("contentPage", "/Pages/Staff/payments.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    private boolean isStaffOrAdmin(Account user) {
        return "Staff".equalsIgnoreCase(user.getRole()) || "Admin".equalsIgnoreCase(user.getRole());
    }
}
