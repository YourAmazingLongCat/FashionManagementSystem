package Controllers;

import Models.Account;
import Models.CartItem;
import Models.Order;
import Services.OrderService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "OrderReviewServlet", urlPatterns = {"/customer/order-review"})
public class OrderReviewServlet extends HttpServlet {

    private static final String LAYOUT_PAGE = "/Pages/Guest/Home/Layout/Layout.jsp";
    private static final String REVIEW_PAGE = "/Pages/Customer/orderReview.jsp";

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderId = trim(request.getParameter("orderId"));
        if (!isEmpty(orderId)) {
            response.sendRedirect(request.getContextPath()
                    + "/customer/order-detail?orderId=" + orderId);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/customer/order-history");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String customerId = getCustomerId(session);
        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("checkoutCart");
        if (cart == null || cart.isEmpty()) {
            cart = (List<CartItem>) session.getAttribute("cart");
        }

        if (cart == null || cart.isEmpty()) {
            session.setAttribute("errorMessage", "Your checkout session has expired.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        String shippingAddress = trim(request.getParameter("shippingAddress"));
        String phone = trim(request.getParameter("phone"));
        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            request.setAttribute("errorMessage",
                    "Please enter the shipping address and phone number.");
            request.setAttribute("contentPage", REVIEW_PAGE);
            request.getRequestDispatcher(LAYOUT_PAGE).forward(request, response);
            return;
        }

        Order orderPreview = orderService.reviewOrder(
                customerId, shippingAddress, phone, cart);
        if (orderPreview == null) {
            session.setAttribute("errorMessage", "Cannot review this order.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        request.setAttribute("orderPreview", orderPreview);
        request.setAttribute("cart", cart);
        request.setAttribute("contentPage", REVIEW_PAGE);
        request.getRequestDispatcher(LAYOUT_PAGE).forward(request, response);
    }

    private String getCustomerId(HttpSession session) {
        Object direct = session.getAttribute("customerId");
        if (direct != null && !direct.toString().trim().isEmpty()) {
            return direct.toString().trim();
        }

        Object user = session.getAttribute("USER");
        if (user instanceof Account) {
            return ((Account) user).getAccountId();
        }

        return null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
