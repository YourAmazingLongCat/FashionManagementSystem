package Controllers;

import DALs.BillDAO;
import Models.Bill;
import Models.BillOrderItem;
import Models.RevenueStat;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Controller cho chức năng Bill Management.
 *
 * Các action (tham số "action" trên querystring):
 *  - (không truyền / "list")  -> xem + tìm kiếm + lọc danh sách hóa đơn
 *  - "detail"                 -> xem + tìm kiếm chi tiết 1 hóa đơn
 *  - "chartData"               -> trả JSON dữ liệu cho biểu đồ doanh thu (gọi bằng AJAX)
 *
 * URL mapping: /BillServlet
 */
@WebServlet(name = "BillServlet", urlPatterns = {"/BillServlet"})
public class BillServlet extends HttpServlet {

    private final BillDAO billDAO = new BillDAO();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // JSP nằm trong Web Pages/Pages/Staff/ theo cấu trúc project hiện tại
    private static final String JSP_BILL_LIST = "/Pages/Staff/billList.jsp";
    private static final String JSP_BILL_DETAIL = "/Pages/Staff/billDetail.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "detail":
                handleBillDetail(request, response);
                break;
            case "chartData":
                handleChartData(request, response);
                break;
            case "list":
            default:
                handleBillList(request, response);
                break;
        }
    }

    /**
     * View bill + Search bill + lọc hóa đơn theo ngày (gộp chung 1 action
     * vì search/filter chỉ là danh sách bill có thêm điều kiện WHERE).
     */
    private void handleBillList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = trimOrNull(request.getParameter("keyword"));
        String paymentStatus = trimOrNull(request.getParameter("paymentStatus"));
        String orderStatus = trimOrNull(request.getParameter("orderStatus"));
        Date fromDate = parseDate(request.getParameter("fromDate"));
        Date toDate = parseDate(request.getParameter("toDate"));

        List<Bill> bills = billDAO.searchBills(keyword, paymentStatus, orderStatus, fromDate, toDate);

        // Tính tổng doanh thu của kết quả đang hiển thị (tiện cho người dùng xem nhanh)
        BigDecimal totalOfList = BigDecimal.ZERO;
        for (Bill b : bills) {
            if (b.getTotalAmount() != null) {
                totalOfList = totalOfList.add(b.getTotalAmount());
            }
        }

        request.setAttribute("bills", bills);
        request.setAttribute("totalOfList", totalOfList);

        // Trả lại các giá trị filter để giữ nguyên trên form sau khi submit
        request.setAttribute("keyword", keyword);
        request.setAttribute("paymentStatus", paymentStatus);
        request.setAttribute("orderStatus", orderStatus);
        request.setAttribute("fromDate", request.getParameter("fromDate"));
        request.setAttribute("toDate", request.getParameter("toDate"));

        request.getRequestDispatcher(JSP_BILL_LIST).forward(request, response);
    }

    /**
     * View bill detail + Search bill detail.
     */
    private void handleBillDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String billId = request.getParameter("billId");
        String keyword = trimOrNull(request.getParameter("keyword"));

        if (billId == null || billId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/BillServlet?action=list");
            return;
        }

        Bill bill = billDAO.getBillById(billId);
        if (bill == null) {
            request.setAttribute("errorMessage", "Không tìm thấy hóa đơn: " + billId);
            request.getRequestDispatcher(JSP_BILL_LIST).forward(request, response);
            return;
        }

        List<BillOrderItem> items = billDAO.searchBillDetails(billId, keyword);

        request.setAttribute("bill", bill);
        request.setAttribute("items", items);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher(JSP_BILL_DETAIL).forward(request, response);
    }

    /**
     * Trả về JSON cho biểu đồ đường tăng trưởng doanh thu.
     * Query params: periodType=day|week|month|year, fromDate, toDate
     * Được gọi bằng fetch()/AJAX từ billList.jsp (giống pattern CommentDataServlet).
     */
    private void handleChartData(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String periodType = trimOrNull(request.getParameter("periodType"));
        if (periodType == null
                || !(periodType.equals("day") || periodType.equals("week")
                     || periodType.equals("month") || periodType.equals("year"))) {
            periodType = "day"; // whitelist, mặc định an toàn
        }

        Date fromDate = parseDate(request.getParameter("fromDate"));
        Date toDate = parseDate(request.getParameter("toDate"));

        List<RevenueStat> stats = billDAO.getRevenueStats(periodType, fromDate, toDate);

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(toJson(stats));
        }
    }

    // ================= Helpers =================

    private String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private Date parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            java.util.Date d = DATE_FORMAT.parse(raw.trim());
            return new Date(d.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Tự build JSON thủ công để không phụ thuộc thư viện ngoài (Gson/Jackson).
     * Nếu project đã có Gson trong pom.xml, có thể thay hàm này bằng
     * new Gson().toJson(stats) cho gọn.
     */
    private String toJson(List<RevenueStat> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < stats.size(); i++) {
            RevenueStat r = stats.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"periodLabel\":\"").append(escapeJson(r.getPeriodLabel())).append("\",");
            sb.append("\"totalRevenue\":").append(r.getTotalRevenue() == null ? 0 : r.getTotalRevenue()).append(",");
            sb.append("\"billCount\":").append(r.getBillCount());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}