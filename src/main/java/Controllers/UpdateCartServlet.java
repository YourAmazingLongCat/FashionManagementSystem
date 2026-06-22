/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.CartItemDAO;
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

            int quantity = 1;

            if (qtyStr != null && !qtyStr.isEmpty()) {
                quantity = Integer.parseInt(qtyStr);
            }

            if (quantity < 1) {
                quantity = 1;
            }

            CartItemDAO dao = new CartItemDAO();
            dao.updateQuantity(cartItemId, quantity);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // QUAN TRỌNG: quay lại cart
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}