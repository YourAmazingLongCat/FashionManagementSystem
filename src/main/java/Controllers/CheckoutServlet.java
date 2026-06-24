package Controllers;

import Models.CartItem;
import Services.OrderService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/customer/checkout"})
public class CheckoutServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
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

        request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/Pages/Authentication/login.jsp");
            return;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            request.setAttribute("errorMessage", "Your cart is empty.");
            request.getRequestDispatcher("/Pages/Customer/Cart.jsp").forward(request, response);
            return;
        }

        String shippingAddress = request.getParameter("shippingAddress");
        String phone = request.getParameter("phone");

        if (isEmpty(shippingAddress) || isEmpty(phone)) {
            request.setAttribute("errorMessage", "Please enter shipping address and phone number.");
            request.setAttribute("shippingAddress", shippingAddress);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
            return;
        }

        String orderId = orderService.checkout(customerId, shippingAddress, phone, cart);

        if (orderId != null) {
            session.removeAttribute("cart");
            response.sendRedirect(request.getContextPath() + "/customer/order-detail?orderId=" + orderId);
            return;
        }

        request.setAttribute("errorMessage", "Checkout failed. Please check your information.");
        request.setAttribute("shippingAddress", shippingAddress);
        request.setAttribute("phone", phone);
        request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
