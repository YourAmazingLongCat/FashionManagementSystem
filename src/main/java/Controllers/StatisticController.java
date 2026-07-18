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
public class StatisticController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        loadDashboard(request);
        request.getRequestDispatcher("/Pages/Admin/Admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StatisticDAO dao = new StatisticDAO();
        loadDashboard(request); // load các dữ liệu cơ bản

        // Lấy tham số minOrders từ form (nếu có)
        String quantityParam = request.getParameter("quantity");
        if (quantityParam != null && !quantityParam.isEmpty()) {
            int quantity = Integer.parseInt(quantityParam);
            request.setAttribute("customerStatistics", dao.searchCustomerByOrderQuantity(quantity));
        }

        request.getRequestDispatcher("/Pages/Admin/Admin.jsp").forward(request, response);
    }

    private void loadDashboard(HttpServletRequest request) {
        StatisticDAO dao = new StatisticDAO();

        // Lấy tham số lọc thời gian từ request
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");

        // Nếu không có tham số thì gọi phương thức không tham số (hoặc có tham số null)
        // Các phương thức mới đều xử lý null an toàn
        request.setAttribute("totalCustomers", dao.getTotalCustomers());
        request.setAttribute("totalOrders", dao.getTotalOrders());

        // Gọi các phương thức mới hỗ trợ lọc thời gian
        request.setAttribute("revenue", dao.getRevenue(fromDate, toDate));
        request.setAttribute("profit", dao.getProfit(fromDate, toDate));
        request.setAttribute("costOfGoodsSold", dao.getCostOfGoodsSold(fromDate, toDate));
        request.setAttribute("totalProductSold", dao.getTotalProductSold(fromDate, toDate));

        // Top 5 sản phẩm bán chạy nhất
        request.setAttribute("topProducts", dao.getTopProducts(5, fromDate, toDate));

        // Chi tiết tất cả sản phẩm đã bán
        request.setAttribute("productSales", dao.getProductSales(fromDate, toDate));

        // Top 10 khách hàng chi tiêu nhiều nhất
        request.setAttribute("topSpenders", dao.getTopSpenders(10, fromDate, toDate));

        // Danh sách khách hàng theo số đơn hàng (có thể giữ nguyên không lọc thời gian,
        // hoặc bạn có thể cải tiến thêm phương thức getTopCustomers có tham số ngày nếu cần)
        // Ở đây tạm thời giữ nguyên getTopCustomers() không lọc thời gian
        request.setAttribute("customerStatistics", dao.getTopCustomers());

        // Thống kê trạng thái đơn hàng
        request.setAttribute("orderStatistics", dao.getOrderStatistics());
    }
}