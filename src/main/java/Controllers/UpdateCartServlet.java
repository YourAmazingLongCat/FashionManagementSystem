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

@WebServlet(name = "UpdateCartServlet", urlPatterns = {"/cart/update"})
public class UpdateCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String cartItemId = request.getParameter("cartItemId");
            String qtyStr = request.getParameter("quantity");

            if (cartItemId == null || cartItemId.isBlank()) {
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid cart item.\"}"
                );
                return;
            }

            int quantity = 1;

            if (qtyStr != null && !qtyStr.isEmpty()) {
                try {
                    quantity = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ignored) {
                    quantity = 1;
                }
            }

            if (quantity < 1) {
                quantity = 1;
            }

            CartItemDAO cartItemDAO = new CartItemDAO();
            ProductDAO productDAO = new ProductDAO();

            String variantId = cartItemDAO.getVariantIdByCartItemId(cartItemId);

            boolean adjusted = false;
            int availableStock = 0;

            if (variantId != null) {

                ProductVariant variant = productDAO.getVariantById(variantId);

                if (variant != null) {

                    availableStock = variant.getAvailableQty();

                    if (quantity > availableStock) {
                        quantity = availableStock;
                        adjusted = true;
                    }
                }
            }

            // Nếu tồn kho = 0
            if (availableStock <= 0) {
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"This product is currently out of stock.\"}"
                );
                return;
            }

            cartItemDAO.updateQuantity(cartItemId, quantity);

            response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"adjusted\":" + adjusted + ","
                    + "\"quantity\":" + quantity + ","
                    + "\"stock\":" + availableStock
                    + "}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unable to update cart.\"}"
            );
        }
    }
}