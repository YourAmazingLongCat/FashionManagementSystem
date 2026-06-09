/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.CartDAO;
import DALs.CartItemDAO;
import Models.Account;
import Models.Cart;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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

            request.setAttribute(
                    "cartItems",
                    itemDAO.getCartItems(cart.getCartId()));

            request.setAttribute(
                    "total",
                    itemDAO.getCartTotal(cart.getCartId()));

        } else {

            request.setAttribute("total", 0);
        }

        request.getRequestDispatcher(
                "/Pages/Customer/Cart.jsp")
                .forward(request, response);
    }
}