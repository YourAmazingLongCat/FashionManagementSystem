/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.CartItemDAO;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Admin
 */


@WebServlet("/cart/delete")
public class DeleteCartItemServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String cartItemId =
                request.getParameter("id");

        CartItemDAO dao =
                new CartItemDAO();

        dao.deleteItem(cartItemId);

        response.sendRedirect(
                request.getContextPath()
                + "/cart");
    }
}