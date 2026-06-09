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
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 *
 * @author Admin
 */



@WebServlet("/cart/add")
public class AddToCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
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

        String variantId = request.getParameter("variantId");
        int quantity = Integer.parseInt(
                request.getParameter("quantity"));

        CartDAO cartDAO = new CartDAO();

        Cart cart = cartDAO.getActiveCart(
                acc.getAccountId());

        String cartId;

        if (cart == null) {
            cartId = cartDAO.createCart(
                    acc.getAccountId());
        } else {
            cartId = cart.getCartId();
        }

        CartItemDAO itemDAO = new CartItemDAO();

        if (itemDAO.existsItem(cartId, variantId)) {

            itemDAO.increaseQuantity(
                    cartId,
                    variantId,
                    quantity);

        } else {

            itemDAO.addItem(
                    cartId,
                    variantId,
                    quantity);
        }

        response.sendRedirect(
                request.getHeader("referer"));
    }
}