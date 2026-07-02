/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controllers;

/**
 *
 * @author Admin
 */
import DALs.StatisticDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/Admin")
public class StatisticController
        extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
                   IOException {

        loadDashboard(request);

        request.getRequestDispatcher(
                "/Pages/Admin/Admin.jsp")
                .forward(request,
                         response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
                   IOException {

        StatisticDAO dao =
                new StatisticDAO();

        loadDashboard(request);

        int quantity =
                Integer.parseInt(
                        request.getParameter(
                                "quantity"));

        request.setAttribute(
                "customerStatistics",
                dao.searchCustomerByOrderQuantity(
                        quantity));

        request.getRequestDispatcher(
                "/Pages/Admin/Admin.jsp")
                .forward(request,
                         response);
    }

    private void loadDashboard(
            HttpServletRequest request) {

        StatisticDAO dao =
                new StatisticDAO();

        request.setAttribute(
                "totalCustomers",
                dao.getTotalCustomers());

        request.setAttribute(
                "totalOrders",
                dao.getTotalOrders());

        request.setAttribute(
                "revenue",
                dao.getRevenue());
        request.setAttribute(
        "profit",
        dao.getProfit());

        request.setAttribute(
                "customerStatistics",
                dao.getTopCustomers());

        request.setAttribute(
                "orderStatistics",
                dao.getOrderStatistics());
    }
}