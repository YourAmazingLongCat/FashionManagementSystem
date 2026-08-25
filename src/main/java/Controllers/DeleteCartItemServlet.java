package Controllers;

import DALs.CartDAO;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/cart/delete")
public class DeleteCartItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String itemId = request.getParameter("id");
        if (itemId != null && !itemId.isBlank()) {
            CartDAO dao = new CartDAO();
            dao.deleteItem(itemId);
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}