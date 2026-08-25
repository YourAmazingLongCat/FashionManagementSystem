package Controllers;

import DALs.CartDAO;
import Models.Account;
import Models.Cart;
import Models.CartItemView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/cart")
public class ViewCartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Account loggedUser = (Account) request.getSession().getAttribute("USER");
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        Cart activeCart = cartDAO.getActiveCart(loggedUser.getAccountId());

        List<CartItemView> items = new ArrayList<>();
        double total = 0.0;
        int cartCount = 0;

        if (activeCart != null) {
            // Xóa các item không hợp lệ (variant đã bị xóa hoặc hết hàng)
            cartDAO.cleanupInvalidItems(activeCart.getCartId());

            items = cartDAO.getCartItems(activeCart.getCartId());
            total = cartDAO.getCartTotal(activeCart.getCartId());
            cartCount = items.stream().mapToInt(CartItemView::getQuantity).sum();
        }

        request.setAttribute("cartItems", items);
        request.setAttribute("total", total);
        request.getSession().setAttribute("cartCount", cartCount);

        request.setAttribute("contentPage", "/Pages/Customer/Cart.jsp");
        request.getRequestDispatcher("/Pages/Guest/Home/Layout/Layout.jsp").forward(request, response);
    }
}