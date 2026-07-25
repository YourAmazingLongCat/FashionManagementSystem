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
import java.util.List;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ManageVoucherServlet", urlPatterns = {"/ManageVoucherServlet", "/manage-voucher"})
public class ManageVoucherServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        VoucherDAO voucherDAO = new VoucherDAO();

        if (action == null) {
            action = "list"; 
        }

        switch (action) {
            case "toggleStatus":
                String identifier = request.getParameter("id");
                String currentStatus = request.getParameter("status");
                
                if (identifier != null && currentStatus != null) {
                    String cleanId = identifier.trim();
                    // Trạng thái chuẩn đã chốt với SQL Server là Active / Inactive
                    String newStatus = "Active".equalsIgnoreCase(currentStatus.trim()) ? "Inactive" : "Active";
                    
                    boolean isSuccess = voucherDAO.toggleVoucherStatus(cleanId, newStatus);
                    
                    if (!isSuccess) {
                        response.setContentType("text/html;charset=UTF-8");
                        response.getWriter().print("<script>alert('LỖI: SQL Server từ chối cập nhật! Vui lòng kiểm tra lại Database.'); window.location.href='" + request.getContextPath() + "/manage-voucher?action=list';</script>");
                        return;
                    }
                }
                
                // 👉 MẸO CHỐNG CACHE 100%: Gắn thêm thời gian thực (t=...) vào đuôi URL
                response.sendRedirect(request.getContextPath() + "/manage-voucher?action=list&t=" + System.currentTimeMillis());
                break;
                
            case "list":
            default:
                List<Voucher> vouchers = voucherDAO.getAllVouchers();
                request.setAttribute("vouchers", vouchers);
                request.getRequestDispatcher("Pages/Admin/manage-voucher.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        VoucherDAO voucherDAO = new VoucherDAO();

        if ("addVoucher".equals(action)) {
            try {
                String voucherCode = request.getParameter("voucherCode");
                String discountType = request.getParameter("discountType");
                
                // Lọc sạch sẽ mọi tạp âm (dấu phẩy, VNĐ, %) trước khi ép kiểu
                String discountValueStr = request.getParameter("discountValue").replaceAll(",", "").replace("VNĐ", "").replace("%", "").trim();
                double discountValue = Double.parseDouble(discountValueStr);
                
                String minOrderStr = request.getParameter("minOrderValue").replaceAll(",", "").replace("VNĐ", "").trim();
                double minOrderValue = Double.parseDouble(minOrderStr);
                
                String maxDiscountStr = request.getParameter("maxDiscount");
                Double maxDiscount = (maxDiscountStr == null || maxDiscountStr.isEmpty()) ? null : Double.parseDouble(maxDiscountStr.replaceAll(",", "").replace("VNĐ", "").trim());
                
                int usageLimit = Integer.parseInt(request.getParameter("usageLimit"));
                String status = request.getParameter("status");
                
                if (status != null && status.trim().equalsIgnoreCase("Deactive")) {
                    status = "Inactive";
                }
                
                String startDateStr = request.getParameter("startDate");
                String endDateStr = request.getParameter("endDate");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                Timestamp startDate = new Timestamp(sdf.parse(startDateStr).getTime());
                Timestamp endDate = new Timestamp(sdf.parse(endDateStr).getTime());

                String generatedId = "VOU" + System.currentTimeMillis();

                Voucher v = new Voucher();
                v.setVoucherId(generatedId);
                v.setVoucherCode(voucherCode.toUpperCase().trim());
                v.setDiscountType(discountType);
                v.setDiscountValue(discountValue);
                v.setMinOrderValue(minOrderValue);
                v.setMaxDiscount(maxDiscount);
                v.setUsageLimit(usageLimit);
                v.setStatus(status.trim());
                v.setStartDate(startDate);
                v.setEndDate(endDate);
                
                boolean isSuccess = voucherDAO.insertVoucher(v);
                
                if (isSuccess) {
                    // 👉 CHỐNG CACHE KHI THÊM MỚI VOUCHER
                    response.sendRedirect(request.getContextPath() + "/manage-voucher?action=list&t=" + System.currentTimeMillis());
                } else {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().print("<script>alert('LỖI DATABASE: Không thể thêm! Có thể mã Code này đã tồn tại.'); history.back();</script>");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().print("<script>alert('LỖI DỮ LIỆU ĐẦU VÀO: Bạn chưa nhập đủ thông tin hoặc sai định dạng (" + e.getMessage() + ")'); history.back();</script>");
            }
        } else {
            doGet(request, response);
        }
    }
}
