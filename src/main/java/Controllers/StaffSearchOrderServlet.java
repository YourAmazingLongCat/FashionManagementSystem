package Controllers;

import Models.Order;
import Services.OrderService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StaffSearchOrderServlet", urlPatterns = {"/staff/search-orders"})
public class StaffSearchOrderServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String keyword = request.getParameter("keyword");
        List<Order> listOrders = orderService.searchOrdersForStaff(keyword);

        request.setAttribute("listOrders", listOrders);
        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.getRequestDispatcher("/Pages/Staff/orders.jsp").forward(request, response);
    }
}
