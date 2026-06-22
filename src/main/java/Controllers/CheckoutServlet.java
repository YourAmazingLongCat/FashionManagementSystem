/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.OrderDAO;
import Models.*;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author CE181629 - Ngo Manh Quan
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = {"/CheckoutServlet"})
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGetCheckout(request, response);
    }

    private void doGetCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        List<Cart> carts = (List<Cart>) session.getAttribute("cart");

        if (carts == null || carts.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        request.getRequestDispatcher("/views/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPostCheckout(request, response);
    }

    private void doPostCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");
        List<Cart> allCarts = (List<Cart>) session.getAttribute("cart");

        // 1. Kiểm tra đăng nhập
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Lấy danh sách ID các sản phẩm được chọn từ UI Giỏ hàng
        String[] selectedItemIds = request.getParameterValues("selectedItems");

        if (allCarts == null || allCarts.isEmpty() || selectedItemIds == null || selectedItemIds.length == 0) {
            session.setAttribute("error", "Vui lòng chọn ít nhất một sản phẩm để thanh toán!");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // 3. Lọc ra danh sách sản phẩm thực sự được thanh toán
        List<Cart> checkoutItems = new ArrayList<>();
        for (Cart item : allCarts) {
            for (String selectedId : selectedItemIds) {
                if (String.valueOf(item.getCartItemId()).equals(selectedId)) {
                    checkoutItems.add(item);
                    break;
                }
            }
        }

        // 4. Lấy thông tin giao hàng từ Form
        String shippingAddress = request.getParameter("shippingAddress");
        String phone = request.getParameter("phone");

        // Validation cơ bản (UX bảo vệ hệ thống)
        if (shippingAddress == null || shippingAddress.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ số điện thoại và địa chỉ nhận hàng!");
            request.setAttribute("checkoutItems", checkoutItems); // Giữ lại danh sách để render lại UI
            request.getRequestDispatcher("/views/checkout.jsp").forward(request, response);
            return;
        }

        OrderDAO orderDAO = new OrderDAO();

        // 5. Tạo đơn hàng và lưu chi tiết
        String orderId = orderDAO.CreateOrder(
                String.valueOf(account.getAccId()),
                "PENDING",
                LocalDateTime.now(),
                shippingAddress,
                phone);

        if (orderId != null && !orderId.trim().isEmpty()) {
            orderDAO.AddOrderItems(orderId, checkoutItems);

            // Cập nhật lại session giỏ hàng (xóa các món đã mua, giữ lại các món chưa mua)
            allCarts.removeAll(checkoutItems);
            session.setAttribute("cart", allCarts);

            // SỬA LỖI: Đã truyền orderId vào URL thành công
            response.sendRedirect(request.getContextPath() + "/OrderReviewServlet?orderId=" + orderId);
        } else {
            request.setAttribute("error", "Đặt hàng thất bại. Vui lòng thử lại!");
            request.setAttribute("checkoutItems", checkoutItems);
            request.getRequestDispatcher("/views/checkout.jsp").forward(request, response);
        }
    }

}
