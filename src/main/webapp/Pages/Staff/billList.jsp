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

    <!-- ===== Chuyển đổi chế độ xem: Doanh thu / Theo sản phẩm ===== -->
    <div class="btn-group mb-3" role="group">
        <button type="button" id="btnModeRevenue" class="btn btn-outline-primary">📈 Doanh thu</button>
        <button type="button" id="btnModeProduct" class="btn btn-outline-primary">📦 Theo sản phẩm</button>
    </div>

    <!-- ===== Biểu đồ tăng trưởng doanh thu ===== -->
    <div class="card p-3 mb-4" id="revenuePanel">
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Biểu đồ tăng trưởng doanh thu</h5>
            <div class="d-flex gap-2 align-items-center">
                <select id="periodType" class="form-select form-select-sm" style="width:130px;">
<!--                    <option value="day">Theo ngày</option>-->
                    <option value="week" ${empty param.chartPeriodType || param.chartPeriodType == 'week' ? 'selected' : ''}>Theo tuần</option>
                    <option value="month" ${param.chartPeriodType == 'month' ? 'selected' : ''}>Theo tháng</option>
                    <option value="year" ${param.chartPeriodType == 'year' ? 'selected' : ''}>Theo năm</option>
                </select>
                <input type="date" id="chartFromDate" class="form-control form-control-sm" style="width:150px;" value="${param.chartFromDate}">
                <input type="date" id="chartToDate" class="form-control form-control-sm" style="width:150px;" value="${param.chartToDate}">
                <button id="btnLoadChart" class="btn btn-sm btn-primary">Xem</button>
            </div>
        </div>
        <canvas id="revenueChart" height="90"></canvas>
    </div>

    <!-- ===== Thống kê theo sản phẩm ===== -->
    <div id="productPanel" style="display:none;">
        <div class="card p-3 mb-4">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
                <h5 class="mb-0">Số lượng hàng bán được (theo sản phẩm)</h5>
                <div class="d-flex gap-2 align-items-center flex-wrap">
                    <select id="productPeriodType" class="form-select form-select-sm" style="width:130px;">
                        <option value="day" ${param.productPeriodType == 'day' ? 'selected' : ''}>Theo ngày</option>
                        <option value="week" ${param.productPeriodType == 'week' ? 'selected' : ''}>Theo tuần</option>
                        <option value="month" ${empty param.productPeriodType || param.productPeriodType == 'month' ? 'selected' : ''}>Theo tháng</option>
                        <option value="year" ${param.productPeriodType == 'year' ? 'selected' : ''}>Theo năm</option>
                    </select>
                    <select id="productFilterSelect" class="form-select form-select-sm" style="width:200px;">
                        <option value="">-- Tất cả sản phẩm --</option>
                        <!-- các option sản phẩm được đổ vào bằng JS (fetch productOptions) -->
                    </select>
                    <input type="date" id="productFromDate" class="form-control form-control-sm" style="width:150px;" value="${param.productFromDate}">
                    <input type="date" id="productToDate" class="form-control form-control-sm" style="width:150px;" value="${param.productToDate}">
                    <button id="btnLoadProduct" class="btn btn-sm btn-primary">Xem</button>
                </div>
            </div>
            <canvas id="productChart" height="90"></canvas>
        </div>

        <!-- ===== Thẻ tổng hợp ===== -->
        <div class="row mb-4">
            <div class="col-md-4">
                <div class="card p-3 text-center">
                    <div class="text-muted small">Tổng số sản phẩm đã bán</div>
                    <div class="fs-4 fw-bold" id="sumTotalQuantity">0</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card p-3 text-center">
                    <div class="text-muted small">Tổng tiền đã thu (Paid)</div>
                    <div class="fs-4 fw-bold text-success" id="sumTotalPaid">0 đ</div>
                    <div class="text-muted small mt-1"><span id="sumPaidQtyCount">0</span> sản phẩm đã thanh toán</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card p-3 text-center">
                    <div class="text-muted small">Tổng tiền còn thiếu</div>
                    <div class="fs-4 fw-bold text-danger" id="sumTotalUnpaid">0 đ</div>
                    <div class="text-muted small mt-1"><span id="sumUnpaidQtyCount">0</span> sản phẩm chưa thanh toán</div>
                </div>
            </div>
        </div>

        <!-- ===== Bảng chi tiết theo sản phẩm ===== -->
        <div class="card p-3 mb-4">
            <h5 class="mb-3">Chi tiết theo sản phẩm</h5>
            <table class="table table-hover align-middle">
                <thead class="table-light">
                <tr>
                    <th>Sản phẩm</th>
                    <th class="text-end">Số lượng bán</th>
                    <th class="text-end">SL đã thanh toán</th>
                    <th class="text-end">Tiền đã thu (Paid)</th>
                    <th class="text-end">SL chưa thanh toán</th>
                    <th class="text-end">Tiền còn thiếu</th>
                </tr>
                </thead>
                <tbody id="productSummaryBody">
                </tbody>
            </table>
        </div>
    </div>

    <!-- ===== Danh sách hóa đơn (form tìm kiếm + bảng) - chỉ hiện ở chế độ Doanh thu ===== -->
    <div id="billListSection">

    <!-- ===== Form tìm kiếm / lọc hóa đơn ===== -->
    <div class="card p-3 mb-4">
        <form method="get" action="${pageContext.request.contextPath}/BillServlet" class="row g-2 align-items-end" id="billSearchForm">
            <input type="hidden" name="action" value="list">

            <!-- Các trường ẩn này dùng để "mang theo" trạng thái bộ lọc của
                 2 biểu đồ phía trên khi form này submit (reload trang), để
                 biểu đồ không bị reset về mặc định sau khi tìm hóa đơn. -->
            <input type="hidden" id="hf_chartMode" name="chartMode" value="${param.chartMode}">
            <input type="hidden" id="hf_chartPeriodType" name="chartPeriodType" value="${param.chartPeriodType}">
            <input type="hidden" id="hf_chartFromDate" name="chartFromDate" value="${param.chartFromDate}">
            <input type="hidden" id="hf_chartToDate" name="chartToDate" value="${param.chartToDate}">
            <input type="hidden" id="hf_productPeriodType" name="productPeriodType" value="${param.productPeriodType}">
            <input type="hidden" id="hf_productFilterId" name="productFilterId" value="${param.productFilterId}">
            <input type="hidden" id="hf_productFromDate" name="productFromDate" value="${param.productFromDate}">
            <input type="hidden" id="hf_productToDate" name="productToDate" value="${param.productToDate}">

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

    </div> <!-- /#billListSection -->
</div>

<script>
let revenueChart = null;
let productChart = null;

const contextPath = '${pageContext.request.contextPath}';

// ================= Biểu đồ doanh thu =================
function loadChart() {
    const periodType = document.getElementById('periodType').value;
    const fromDate = document.getElementById('chartFromDate').value;
    const toDate = document.getElementById('chartToDate').value;

    const params = new URLSearchParams({ action: 'chartData', periodType });
    if (fromDate) params.append('fromDate', fromDate);
    if (toDate) params.append('toDate', toDate);

    fetch(contextPath + '/BillServlet?' + params.toString())
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

// ================= Dropdown sản phẩm =================
function loadProductOptions() {
    return fetch(contextPath + '/BillServlet?action=productOptions')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('productFilterSelect');
            data.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.productId;
                opt.textContent = p.productName;
                select.appendChild(opt);
            });
            const savedProductId = '${param.productFilterId}';
            if (savedProductId) {
                select.value = savedProductId;
            }
        })
        .catch(err => console.error('Lỗi tải danh sách sản phẩm:', err));
}

// ================= Biểu đồ + bảng thống kê theo sản phẩm =================
function loadProductData() {
    const periodType = document.getElementById('productPeriodType').value;
    const productId = document.getElementById('productFilterSelect').value;
    const fromDate = document.getElementById('productFromDate').value;
    const toDate = document.getElementById('productToDate').value;

    // ---- biểu đồ số lượng bán ----
    const chartParams = new URLSearchParams({ action: 'productChartData', periodType });
    if (productId) chartParams.append('productId', productId);
    if (fromDate) chartParams.append('fromDate', fromDate);
    if (toDate) chartParams.append('toDate', toDate);

    fetch(contextPath + '/BillServlet?' + chartParams.toString())
        .then(res => res.json())
        .then(data => {
            const labels = data.map(d => d.periodLabel);
            const qty = data.map(d => d.quantitySold);
            const revenue = data.map(d => d.revenuePaid);

            const ctx = document.getElementById('productChart').getContext('2d');
            if (productChart) productChart.destroy();

            productChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Số lượng bán ra',
                        data: qty,
                        backgroundColor: 'rgba(25,135,84,0.6)'
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                afterLabel: (item) => 'Tiền đã thu: ' + Number(revenue[item.dataIndex]).toLocaleString('vi-VN') + ' đ'
                            }
                        }
                    },
                    scales: {
                        y: { beginAtZero: true, ticks: { precision: 0 } }
                    }
                }
            });
        })
        .catch(err => console.error('Lỗi tải biểu đồ sản phẩm:', err));

    // ---- thẻ tổng hợp + bảng chi tiết ----
    const summaryParams = new URLSearchParams({ action: 'productSummary' });
    if (productId) summaryParams.append('productId', productId);
    if (fromDate) summaryParams.append('fromDate', fromDate);
    if (toDate) summaryParams.append('toDate', toDate);

    fetch(contextPath + '/BillServlet?' + summaryParams.toString())
        .then(res => res.json())
        .then(data => {
            document.getElementById('sumTotalQuantity').textContent = Number(data.totalQuantity).toLocaleString('vi-VN');
            document.getElementById('sumTotalPaid').textContent = Number(data.totalPaidAmount).toLocaleString('vi-VN') + ' đ';
            document.getElementById('sumTotalUnpaid').textContent = Number(data.totalUnpaidAmount).toLocaleString('vi-VN') + ' đ';
            document.getElementById('sumPaidQtyCount').textContent = Number(data.totalPaidQuantity).toLocaleString('vi-VN');
            document.getElementById('sumUnpaidQtyCount').textContent = Number(data.totalUnpaidQuantity).toLocaleString('vi-VN');

            const tbody = document.getElementById('productSummaryBody');
            tbody.innerHTML = '';

            if (!data.rows || data.rows.length === 0) {
                const tr = document.createElement('tr');
                const td = document.createElement('td');
                td.colSpan = 6;
                td.className = 'text-center text-muted';
                td.textContent = 'Không có dữ liệu.';
                tr.appendChild(td);
                tbody.appendChild(tr);
                return;
            }

            data.rows.forEach(r => {
                const tr = document.createElement('tr');

                const tdName = document.createElement('td');
                tdName.textContent = r.productName;

                const tdQty = document.createElement('td');
                tdQty.className = 'text-end';
                tdQty.textContent = Number(r.totalQuantity).toLocaleString('vi-VN');

                const tdPaidQty = document.createElement('td');
                tdPaidQty.className = 'text-end';
                tdPaidQty.textContent = Number(r.paidQuantity).toLocaleString('vi-VN');

                const tdUnpaidQty = document.createElement('td');
                tdUnpaidQty.className = 'text-end';
                tdUnpaidQty.textContent = Number(r.unpaidQuantity).toLocaleString('vi-VN');

                const tdPaid = document.createElement('td');
                tdPaid.className = 'text-end';
                tdPaid.textContent = Number(r.totalRevenuePaid).toLocaleString('vi-VN') + ' đ';

                const tdUnpaid = document.createElement('td');
                tdUnpaid.className = 'text-end';
                tdUnpaid.textContent = Number(r.totalUnpaidAmount).toLocaleString('vi-VN') + ' đ';

                tr.append(tdName, tdQty, tdPaidQty, tdPaid, tdUnpaidQty, tdUnpaid);
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Lỗi tải thống kê sản phẩm:', err));
}

// ================= Chuyển đổi chế độ xem =================
function switchMode(mode) {
    document.getElementById('revenuePanel').style.display = mode === 'revenue' ? '' : 'none';
    document.getElementById('productPanel').style.display = mode === 'product' ? '' : 'none';
    // Danh sách hóa đơn (form tìm kiếm + bảng) chỉ hiện ở chế độ Doanh thu
    document.getElementById('billListSection').style.display = mode === 'revenue' ? '' : 'none';
    document.getElementById('btnModeRevenue').classList.toggle('active', mode === 'revenue');
    document.getElementById('btnModeProduct').classList.toggle('active', mode === 'product');
    document.getElementById('hf_chartMode').value = mode;

    if (mode === 'product') {
        loadProductData();
    }
}

document.getElementById('btnModeRevenue').addEventListener('click', () => switchMode('revenue'));
document.getElementById('btnModeProduct').addEventListener('click', () => switchMode('product'));
document.getElementById('btnLoadChart').addEventListener('click', loadChart);
document.getElementById('periodType').addEventListener('change', loadChart);
document.getElementById('btnLoadProduct').addEventListener('click', loadProductData);

// Trước khi submit form tìm kiếm hóa đơn phía dưới, đồng bộ trạng thái
// hiện tại của 2 biểu đồ vào các trường hidden để mang theo qua querystring
// -> sau khi trang reload, biểu đồ khôi phục lại đúng bộ lọc cũ.
document.getElementById('billSearchForm').addEventListener('submit', function () {
    document.getElementById('hf_chartPeriodType').value = document.getElementById('periodType').value;
    document.getElementById('hf_chartFromDate').value = document.getElementById('chartFromDate').value;
    document.getElementById('hf_chartToDate').value = document.getElementById('chartToDate').value;
    document.getElementById('hf_productPeriodType').value = document.getElementById('productPeriodType').value;
    document.getElementById('hf_productFilterId').value = document.getElementById('productFilterSelect').value;
    document.getElementById('hf_productFromDate').value = document.getElementById('productFromDate').value;
    document.getElementById('hf_productToDate').value = document.getElementById('productToDate').value;
});

// ================= Khởi tạo khi mở trang =================
const initialMode = '${empty param.chartMode ? "revenue" : param.chartMode}';

// QUAN TRỌNG: phải set display cho CẢ HAI panel (kể cả panel đang active),
// vì productPanel có sẵn style="display:none" trong HTML ban đầu -> nếu
// không set lại "display: ''" thì canvas nằm trong div ẩn, Chart.js không
// đo được kích thước nên vẽ biểu đồ ra không hiển thị được.
document.getElementById('revenuePanel').style.display = initialMode === 'revenue' ? '' : 'none';
document.getElementById('productPanel').style.display = initialMode === 'product' ? '' : 'none';
document.getElementById('billListSection').style.display = initialMode === 'revenue' ? '' : 'none';
document.getElementById('btnModeRevenue').classList.toggle('active', initialMode === 'revenue');
document.getElementById('btnModeProduct').classList.toggle('active', initialMode === 'product');
document.getElementById('hf_chartMode').value = initialMode;

loadChart();
loadProductOptions().then(() => {
    if (initialMode === 'product') {
        loadProductData();
    }
});
</script>
</body>
</html>
