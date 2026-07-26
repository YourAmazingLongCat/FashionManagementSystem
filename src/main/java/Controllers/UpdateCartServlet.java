/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.CartItemDAO;
import DALs.ProductDAO;
import Models.ProductVariant;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 *
 * @author Admin
 */

@WebServlet(name = "UpdateCartServlet", urlPatterns = {"/cart/update"})
public class UpdateCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String cartItemId = request.getParameter("cartItemId");
            String qtyStr = request.getParameter("quantity");

            if (cartItemId == null || cartItemId.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            int quantity = 1;
            if (qtyStr != null && !qtyStr.isEmpty()) {
                try {
                    quantity = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ignored) {}
            }

            if (quantity < 1) {
                quantity = 1;
            }

            CartItemDAO cartItemDAO = new CartItemDAO();
            ProductDAO productDAO = new ProductDAO();

            String variantId = cartItemDAO.getVariantIdByCartItemId(cartItemId);
            if (variantId != null) {
                ProductVariant variant = productDAO.getVariantById(variantId);
                if (variant != null) {
                    int availableStock = variant.getAvailableQty();
                    if (quantity > availableStock) {
                        quantity = availableStock;
                    }
                }
            }

            if (quantity >= 1) {
                cartItemDAO.updateQuantity(cartItemId, quantity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}