package Controllers;

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

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
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

        if (shippingAddress == null || shippingAddress.trim().isEmpty()
                || phone == null || phone.trim().isEmpty()) {

            request.setAttribute("errorMessage", "Please enter shipping address and phone number.");
            request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
            return;
        }

        Order orderPreview = orderService.reviewOrder(
                customerId,
                shippingAddress.trim(),
                phone.trim(),
                cart
        );

        if (orderPreview == null) {
            request.setAttribute("errorMessage", "Cannot review order. Please check your cart.");
            request.getRequestDispatcher("/Pages/Customer/checkout.jsp").forward(request, response);
            return;
        }

        request.setAttribute("orderPreview", orderPreview);
        request.setAttribute("cart", cart);

        request.getRequestDispatcher("/Pages/Customer/orderReview.jsp").forward(request, response);
    }
}