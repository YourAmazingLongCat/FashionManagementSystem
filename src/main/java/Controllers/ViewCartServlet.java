/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
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

/**
 *
 * @author Admin
 */
@WebServlet("/cart")
public class ViewCartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Account acc = (Account) request.getSession()
                .getAttribute("USER");

        if (acc == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        Cart cart = cartDAO.getActiveCart(acc.getAccountId());

        CartItemDAO itemDAO = new CartItemDAO();

        if (cart != null) {
            // Clean up invalid cart items (variant deleted by staff)
            itemDAO.cleanupInvalidItems(cart.getCartId());

            List<CartItemView> items = itemDAO.getCartItems(cart.getCartId());
            request.setAttribute("cartItems", items);
            request.setAttribute("total", itemDAO.getCartTotal(cart.getCartId()));

            // Update cart count in session for header display
            int cartCount = items.stream().mapToInt(CartItemView::getQuantity).sum();
            request.getSession().setAttribute("cartCount", cartCount);

        } else {
            request.setAttribute("cartItems", new ArrayList<>());
            request.setAttribute("total", 0);
            request.getSession().setAttribute("cartCount", 0);
        }

        request.getRequestDispatcher(
                "/Pages/Customer/Cart.jsp")
                .forward(request, response);
    }
}