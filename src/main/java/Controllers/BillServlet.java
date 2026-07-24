package Controllers;

import DALs.BillDAO;
import Models.Bill;
import Models.BillOrderItem;
import Models.ProductOption;
import Models.ProductSaleStat;
import Models.ProductSalesRow;
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
 *  - "productOptions"          -> trả JSON danh sách sản phẩm (đổ dropdown lọc)
 *  - "productChartData"        -> trả JSON dữ liệu biểu đồ số lượng bán theo sản phẩm
 *  - "productSummary"          -> trả JSON bảng + tổng hợp chi tiết theo sản phẩm
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
            case "productOptions":
                handleProductOptions(request, response);
                break;
            case "productChartData":
                handleProductChartData(request, response);
                break;
            case "productSummary":
                handleProductSummary(request, response);
                break;
            case "list":
            default:
                handleBillList(request, response);
                break;
        }
    }

    /**
     * View bill + Search bill + lọc hóa đơn.
     * NOTE: Bills do not have date field, so no date filtering for bill list.
     */
    private void handleBillList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = trimOrNull(request.getParameter("keyword"));
        String paymentStatus = trimOrNull(request.getParameter("paymentStatus"));
        String orderStatus = trimOrNull(request.getParameter("orderStatus"));

        int page = 1;
        int pageSize = 10;
        try {
            if (request.getParameter("page") != null) {
                page = Math.max(1, Integer.parseInt(request.getParameter("page")));
            }
            if (request.getParameter("pageSize") != null) {
                pageSize = Math.max(5, Math.min(50, Integer.parseInt(request.getParameter("pageSize"))));
            }
        } catch (NumberFormatException ignored) {}

        int totalBills = billDAO.countBills(keyword, paymentStatus, orderStatus);
        int totalPages = (int) Math.ceil((double) totalBills / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * pageSize;
        List<Bill> bills = billDAO.searchBillsPaginated(keyword, paymentStatus, orderStatus, offset, pageSize);

        // Tính tổng doanh thu của kết quả đang hiển thị (tiện cho người dùng xem nhanh)
        BigDecimal totalOfList = BigDecimal.ZERO;
        for (Bill b : bills) {
            if (b.getTotalAmount() != null) {
                totalOfList = totalOfList.add(b.getTotalAmount());
            }
        }

        request.setAttribute("bills", bills);
        request.setAttribute("totalOfList", totalOfList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalBills", totalBills);

        // Trả lại các giá trị filter để giữ nguyên trên form sau khi submit
        request.setAttribute("keyword", keyword);
        request.setAttribute("paymentStatus", paymentStatus);
        request.setAttribute("orderStatus", orderStatus);

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

    /**
     * Trả JSON danh sách sản phẩm (id + tên) để đổ vào dropdown lọc theo
     * sản phẩm ở màn hình thống kê "Theo sản phẩm".
     */
    private void handleProductOptions(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        List<ProductOption> options = billDAO.getAllProductOptions();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < options.size(); i++) {
            ProductOption o = options.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"productId\":\"").append(escapeJson(o.getProductId())).append("\",");
            sb.append("\"productName\":\"").append(escapeJson(o.getProductName())).append("\"");
            sb.append("}");
        }
        sb.append("]");

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(sb.toString());
        }
    }

    /**
     * Trả JSON cho biểu đồ "Số lượng hàng bán được" theo sản phẩm.
     * Query params: periodType=day|week|month|year, fromDate, toDate,
     * productId (bỏ trống/không truyền = tất cả sản phẩm).
     */
    private void handleProductChartData(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String periodType = trimOrNull(request.getParameter("periodType"));
        if (periodType == null
                || !(periodType.equals("day") || periodType.equals("week")
                     || periodType.equals("month") || periodType.equals("year"))) {
            periodType = "month"; // whitelist, mặc định an toàn
        }

        Date fromDate = parseDate(request.getParameter("fromDate"));
        Date toDate = parseDate(request.getParameter("toDate"));
        String productId = trimOrNull(request.getParameter("productId"));

        List<ProductSaleStat> stats = billDAO.getProductSalesChart(periodType, fromDate, toDate, productId);

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < stats.size(); i++) {
            ProductSaleStat s = stats.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"periodLabel\":\"").append(escapeJson(s.getPeriodLabel())).append("\",");
            sb.append("\"quantitySold\":").append(s.getQuantitySold()).append(",");
            sb.append("\"revenuePaid\":").append(s.getRevenuePaid() == null ? 0 : s.getRevenuePaid());
            sb.append("}");
        }
        sb.append("]");

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(sb.toString());
        }
    }

    /**
     * Trả JSON bảng chi tiết + tổng hợp theo sản phẩm (tổng số lượng bán,
     * tổng tiền đã thu Paid, tổng tiền còn thiếu) trong khoảng thời gian
     * đang lọc. Query params: fromDate, toDate, productId (bỏ trống = tất cả).
     */
    private void handleProductSummary(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Date fromDate = parseDate(request.getParameter("fromDate"));
        Date toDate = parseDate(request.getParameter("toDate"));
        String productId = trimOrNull(request.getParameter("productId"));

        List<ProductSalesRow> rows = billDAO.getProductSalesSummary(fromDate, toDate, productId);

        int totalQuantity = 0;
        int totalPaidQuantity = 0;
        int totalUnpaidQuantity = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalUnpaid = BigDecimal.ZERO;

        StringBuilder rowsJson = new StringBuilder();
        rowsJson.append("[");
        for (int i = 0; i < rows.size(); i++) {
            ProductSalesRow r = rows.get(i);
            totalQuantity += r.getTotalQuantity();
            totalPaidQuantity += r.getPaidQuantity();
            totalUnpaidQuantity += r.getUnpaidQuantity();
            totalPaid = totalPaid.add(r.getTotalRevenuePaid() == null ? BigDecimal.ZERO : r.getTotalRevenuePaid());
            totalUnpaid = totalUnpaid.add(r.getTotalUnpaidAmount() == null ? BigDecimal.ZERO : r.getTotalUnpaidAmount());

            if (i > 0) rowsJson.append(",");
            rowsJson.append("{");
            rowsJson.append("\"productId\":\"").append(escapeJson(r.getProductId())).append("\",");
            rowsJson.append("\"productName\":\"").append(escapeJson(r.getProductName())).append("\",");
            rowsJson.append("\"totalQuantity\":").append(r.getTotalQuantity()).append(",");
            rowsJson.append("\"paidQuantity\":").append(r.getPaidQuantity()).append(",");
            rowsJson.append("\"unpaidQuantity\":").append(r.getUnpaidQuantity()).append(",");
            rowsJson.append("\"totalRevenuePaid\":").append(r.getTotalRevenuePaid()).append(",");
            rowsJson.append("\"totalUnpaidAmount\":").append(r.getTotalUnpaidAmount());
            rowsJson.append("}");
        }
        rowsJson.append("]");

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"rows\":").append(rowsJson).append(",");
        json.append("\"totalQuantity\":").append(totalQuantity).append(",");
        json.append("\"totalPaidAmount\":").append(totalPaid).append(",");
        json.append("\"totalUnpaidAmount\":").append(totalUnpaid).append(",");
        json.append("\"totalPaidQuantity\":").append(totalPaidQuantity).append(",");
        json.append("\"totalUnpaidQuantity\":").append(totalUnpaidQuantity);
        json.append("}");

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(json.toString());
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