package Controllers;

import DALs.BillDAO;
import Models.Account;
import Models.Bill;
import Models.Order;
import Models.OrderItem;
import Services.OrderService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CustomerOrderDetailServlet", urlPatterns = {"/customer/order-detail"})
public class CustomerOrderDetailServlet extends HttpServlet {

    private OrderService orderService;
    private BillDAO billDAO;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        billDAO = new BillDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderId = request.getParameter("orderId");

        if (orderId == null || orderId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history");
            return;
        }

        Order order = orderService.viewOrderDetailForCustomer(customerId, orderId.trim());

        if (order == null) {
            request.setAttribute("errorMessage", "Order not found.");
            request.setAttribute("contentPage", "/Pages/Customer/orderDetail.jsp");
            request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
            return;
        }

        List<OrderItem> orderItems = orderService.viewOrderItemsForCustomer(customerId, orderId.trim());
        Bill bill = billDAO.getBillByOrderId(orderId.trim());

        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("bill", bill);
        request.setAttribute("contentPage", "/Pages/Customer/orderDetail.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
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
