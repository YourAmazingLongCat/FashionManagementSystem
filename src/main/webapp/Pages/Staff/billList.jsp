<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý hóa đơn</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <style>
        body { background:#f6f7fb; }
        .card { border:none; border-radius:14px; box-shadow:0 2px 10px rgba(0,0,0,.06); }
        .badge-Paid { background:#198754; }
        .badge-Pending { background:#ffc107; color:#333; }
        .badge-Failed { background:#dc3545; }
        .badge-Refunded { background:#6c757d; }
    </style>
</head>
<body class="p-4">
<div class="container-fluid">

    <h3 class="mb-4">Quản lý hóa đơn (Bill Management)</h3>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <!-- ===== Biểu đồ tăng trưởng doanh thu ===== -->
    <div class="card p-3 mb-4">
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Biểu đồ tăng trưởng doanh thu</h5>
            <div class="d-flex gap-2 align-items-center">
                <select id="periodType" class="form-select form-select-sm" style="width:130px;">
                    <option value="day">Theo ngày</option>
                    <option value="week">Theo tuần</option>
                    <option value="month">Theo tháng</option>
                    <option value="year">Theo năm</option>
                </select>
                <input type="date" id="chartFromDate" class="form-control form-control-sm" style="width:150px;">
                <input type="date" id="chartToDate" class="form-control form-control-sm" style="width:150px;">
                <button id="btnLoadChart" class="btn btn-sm btn-primary">Xem</button>
            </div>
        </div>
        <canvas id="revenueChart" height="90"></canvas>
    </div>

    <!-- ===== Form tìm kiếm / lọc hóa đơn ===== -->
    <div class="card p-3 mb-4">
        <form method="get" action="${pageContext.request.contextPath}/BillServlet" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="list">

            <div class="col-md-3">
                <label class="form-label">Từ khóa</label>
                <input type="text" name="keyword" value="${keyword}" class="form-control"
                       placeholder="Mã HĐ, mã ĐH, tên KH, SĐT...">
            </div>

            <div class="col-md-2">
                <label class="form-label">Trạng thái thanh toán</label>
                <select name="paymentStatus" class="form-select">
                    <option value="">-- Tất cả --</option>
                    <c:forEach var="s" items="${['Pending','Paid','Failed','Refunded']}">
                        <option value="${s}" ${paymentStatus == s ? 'selected' : ''}>${s}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-md-2">
                <label class="form-label">Trạng thái đơn hàng</label>
                <select name="orderStatus" class="form-select">
                    <option value="">-- Tất cả --</option>
                    <c:forEach var="s" items="${['Pending','Confirmed','Processing','Shipping','Delivered','Cancelled']}">
                        <option value="${s}" ${orderStatus == s ? 'selected' : ''}>${s}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-md-2">
                <label class="form-label">Từ ngày</label>
                <input type="date" name="fromDate" value="${fromDate}" class="form-control">
            </div>

            <div class="col-md-2">
                <label class="form-label">Đến ngày</label>
                <input type="date" name="toDate" value="${toDate}" class="form-control">
            </div>

            <div class="col-md-1 d-grid">
                <button type="submit" class="btn btn-primary">Lọc</button>
            </div>
        </form>
    </div>

    <!-- ===== Danh sách hóa đơn ===== -->
    <div class="card p-3">
        <div class="d-flex justify-content-between align-items-center mb-2">
            <h5 class="mb-0">Danh sách hóa đơn (${bills.size()} kết quả)</h5>
            <div class="fw-bold">
                Tổng: <fmt:formatNumber value="${totalOfList}" type="number" groupingUsed="true"/> đ
            </div>
        </div>

        <table class="table table-hover align-middle">
            <thead class="table-light">
            <tr>
                <th>Mã hóa đơn</th>
                <th>Mã đơn hàng</th>
                <th>Khách hàng</th>
                <th>SĐT</th>
                <th>Phương thức TT</th>
                <th>Trạng thái TT</th>
                <th>Trạng thái đơn</th>
                <th>Ngày lập</th>
                <th>Tổng tiền</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="b" items="${bills}">
                <tr>
                    <td>${b.billId}</td>
                    <td>${b.orderId}</td>
                    <td>${b.customerName}</td>
                    <td>${b.customerPhone}</td>
                    <td>${b.paymentMethod}</td>
                    <td><span class="badge badge-${b.paymentStatus}">${b.paymentStatus}</span></td>
                    <td>${b.orderStatus}</td>
                    <td><fmt:formatDate value="${b.issuedDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                    <td><fmt:formatNumber value="${b.totalAmount}" type="number" groupingUsed="true"/> đ</td>
                    <td>
                        <a class="btn btn-sm btn-outline-primary"
                           href="${pageContext.request.contextPath}/BillServlet?action=detail&billId=${b.billId}">
                            Xem chi tiết
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty bills}">
                <tr><td colspan="10" class="text-center text-muted">Không có hóa đơn nào phù hợp.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<script>
let revenueChart = null;

function loadChart() {
    const periodType = document.getElementById('periodType').value;
    const fromDate = document.getElementById('chartFromDate').value;
    const toDate = document.getElementById('chartToDate').value;

    const params = new URLSearchParams({ action: 'chartData', periodType });
    if (fromDate) params.append('fromDate', fromDate);
    if (toDate) params.append('toDate', toDate);

    fetch('${pageContext.request.contextPath}/BillServlet?' + params.toString())
        .then(res => res.json())
        .then(data => {
            const labels = data.map(d => d.periodLabel);
            const values = data.map(d => d.totalRevenue);

            const ctx = document.getElementById('revenueChart').getContext('2d');
            if (revenueChart) revenueChart.destroy();

            revenueChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Doanh thu',
                        data: values,
                        borderColor: '#0d6efd',
                        backgroundColor: 'rgba(13,110,253,0.15)',
                        tension: 0.3,
                        fill: true,
                        pointRadius: 3
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, ticks: { callback: v => v.toLocaleString('vi-VN') } }
                    }
                }
            });
        })
        .catch(err => console.error('Lỗi tải dữ liệu biểu đồ:', err));
}

document.getElementById('btnLoadChart').addEventListener('click', loadChart);
document.getElementById('periodType').addEventListener('change', loadChart);

// Tự load biểu đồ lần đầu khi mở trang
loadChart();
</script>
</body>
</html>
