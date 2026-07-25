/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controllers;

import DALs.VoucherDAO;
import Models.Voucher;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ApplyVoucherServlet", urlPatterns = {"/ApplyVoucherServlet"})
public class ApplyVoucherServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String code = request.getParameter("code");
        String cartTotalStr = request.getParameter("cartTotal");

        try {
            double cartTotal = Double.parseDouble(cartTotalStr);
            VoucherDAO voucherDAO = new VoucherDAO();
            
            Voucher v = voucherDAO.getVoucherByCode(code);
            
            String validationMsg = voucherDAO.checkVoucherValidity(v, cartTotal);
            
            if (!"VALID".equals(validationMsg)) {
                out.print("{\"status\":\"ERROR\", \"message\":\"" + validationMsg + "\"}");
                return;
            }
            
            double discountAmount = 0;
            if ("FIXED_AMOUNT".equals(v.getDiscountType())) {
                discountAmount = v.getDiscountValue();
            } else if ("PERCENTAGE".equals(v.getDiscountType())) {
                discountAmount = cartTotal * (v.getDiscountValue() / 100.0);
                if (v.getMaxDiscount() != null && discountAmount > v.getMaxDiscount()) {
                    discountAmount = v.getMaxDiscount();
                }
            }
            
            if (discountAmount > cartTotal) {
                discountAmount = cartTotal;
            }

            out.print("{\"status\":\"SUCCESS\", "
                    + "\"message\":\"Bạn được giảm " + String.format("%,.0f", discountAmount) + "đ\", "
                    + "\"discountAmount\":" + discountAmount + ", "
                    + "\"voucherId\":\"" + v.getVoucherId() + "\"}");

        } catch (Exception e) {
            out.print("{\"status\":\"ERROR\", \"message\":\"Lỗi xử lý dữ liệu!\"}");
        }
    }
}
