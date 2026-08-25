package Controllers;

import DALs.CartDAO;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String cartItemId = request.getParameter("cartItemId");
            String quantityParam = request.getParameter("quantity");

            if (cartItemId == null || cartItemId.isBlank()) {
                response.getWriter().write("{\"success\":false,\"message\":\"Invalid cart item.\"}");
                return;
            }

            int newQuantity = 1;
            if (quantityParam != null && !quantityParam.isEmpty()) {
                try {
                    newQuantity = Integer.parseInt(quantityParam);
                } catch (NumberFormatException ignored) {
                    // keep default
                }
            }
            newQuantity = Math.max(1, newQuantity);

            CartDAO cartDAO = new CartDAO();
            ProductDAO productDAO = new ProductDAO();

            String variantId = cartDAO.getVariantIdByCartItemId(cartItemId);
            boolean adjusted = false;
            int available = 0;

            if (variantId != null) {
                ProductVariant variant = productDAO.getVariantById(variantId);
                if (variant != null) {
                    available = variant.getAvailableQty();
                    if (newQuantity > available) {
                        newQuantity = Math.max(0, available);
                        adjusted = true;
                    }
                }
            }

            if (available <= 0) {
                response.getWriter().write("{\"success\":false,\"message\":\"This product is currently out of stock.\"}");
                return;
            }

            cartDAO.updateQuantity(cartItemId, newQuantity);

            // Build JSON response
            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"success\":true,")
                .append("\"adjusted\":").append(adjusted).append(",")
                .append("\"quantity\":").append(newQuantity).append(",")
                .append("\"stock\":").append(available)
                .append("}");
            response.getWriter().write(json.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            response.getWriter().write("{\"success\":false,\"message\":\"Unable to update cart.\"}");
        }
    }
}