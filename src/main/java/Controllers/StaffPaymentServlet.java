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

        int page = 1;
        int pageSize = 10;
        try {
            if (request.getParameter("page") != null) {
                page = Math.max(1, Integer.parseInt(request.getParameter("page")));
            }
            if (request.getParameter("pageSize") != null) {
                pageSize = Math.max(5, Math.min(50, Integer.parseInt(request.getParameter("pageSize"))));
            }
        } catch (NumberFormatException ignored) {}

        int totalPayments = paymentService.countAllPayments();
        int totalPages = (int) Math.ceil((double) totalPayments / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * pageSize;
        List<Payment> payments = paymentService.getAllPaymentsPaginated(offset, pageSize);
        List<Payment> pendingDeposits = paymentService.getPendingDeposits();

        request.setAttribute("payments", payments);
        request.setAttribute("pendingDeposits", pendingDeposits);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPayments", totalPayments);
        request.setAttribute("contentPage", "/Pages/Staff/payments.jsp");
        request.setAttribute("hideStaffHeader", "true");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }

    private boolean isStaffOrAdmin(Account user) {
        return "Staff".equalsIgnoreCase(user.getRole()) || "Admin".equalsIgnoreCase(user.getRole());
    }
}
