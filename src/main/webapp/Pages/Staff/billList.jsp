<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bill Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
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

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="mb-0">Bill Management</h3>
        <a href="${pageContext.request.contextPath}/staff/products" class="btn btn-outline-secondary btn-sm">Back to Product Management</a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <!-- ===== View mode toggle: Revenue / By Product ===== -->
    <div class="btn-group mb-3" role="group">
        <button type="button" id="btnModeRevenue" class="btn btn-outline-primary">📈 Revenue</button>
        <button type="button" id="btnModeProduct" class="btn btn-outline-primary">📦 By Product</button>
    </div>

    <!-- ===== Revenue Chart ===== -->
    <div class="card p-3 mb-4" id="revenuePanel">
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Revenue Growth Chart</h5>
            <div class="d-flex gap-2 align-items-center">
                <select id="periodType" class="form-select form-select-sm" style="width:130px;">
                    <option value="week" ${empty param.chartPeriodType || param.chartPeriodType == 'week' ? 'selected' : ''}>By Week</option>
                    <option value="month" ${param.chartPeriodType == 'month' ? 'selected' : ''}>By Month</option>
                    <option value="year" ${param.chartPeriodType == 'year' ? 'selected' : ''}>By Year</option>
                </select>
                <input type="date" id="chartFromDate" class="form-control form-control-sm" style="width:150px;" value="${param.chartFromDate}">
                <input type="date" id="chartToDate" class="form-control form-control-sm" style="width:150px;" value="${param.chartToDate}">
                <button id="btnLoadChart" class="btn btn-sm btn-primary">View</button>
            </div>
        </div>
    </div>

    <!-- ===== By Product Stats ===== -->
    <div id="productPanel" style="display:none;">
        <div class="card p-3 mb-4">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
                <h5 class="mb-0">Products Sold (by product)</h5>
                <div class="d-flex gap-2 align-items-center flex-wrap">
                    <select id="productPeriodType" class="form-select form-select-sm" style="width:130px;">
                        <option value="day" ${param.productPeriodType == 'day' ? 'selected' : ''}>By Day</option>
                        <option value="week" ${param.productPeriodType == 'week' ? 'selected' : ''}>By Week</option>
                        <option value="month" ${empty param.productPeriodType || param.productPeriodType == 'month' ? 'selected' : ''}>By Month</option>
                        <option value="year" ${param.productPeriodType == 'year' ? 'selected' : ''}>By Year</option>
                    </select>
                    <select id="productFilterSelect" class="form-select form-select-sm" style="width:200px;">
                        <option value="">-- All Products --</option>
                    </select>
                    <input type="date" id="productFromDate" class="form-control form-control-sm" style="width:150px;" value="${param.productFromDate}">
                    <input type="date" id="productToDate" class="form-control form-control-sm" style="width:150px;" value="${param.productToDate}">
                    <button id="btnLoadProduct" class="btn btn-sm btn-primary">View</button>
                </div>
            </div>
        </div>

        <!-- ===== Summary Cards ===== -->
        <div class="row mb-4">
            <div class="col-md-4">
                <div class="card p-3 text-center">
                    <div class="text-muted small">Total Products Sold</div>
                    <div class="fs-4 fw-bold" id="sumTotalQuantity">0</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card p-3 text-center">
                    <div class="text-muted small">Total Revenue (Paid)</div>
                    <div class="fs-4 fw-bold text-success" id="sumTotalPaid">0 VND</div>
                    <div class="text-muted small mt-1"><span id="sumPaidQtyCount">0</span> products paid</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card p-3 text-center">
                    <div class="text-muted small">Total Outstanding</div>
                    <div class="fs-4 fw-bold text-danger" id="sumTotalUnpaid">0 VND</div>
                    <div class="text-muted small mt-1"><span id="sumUnpaidQtyCount">0</span> products unpaid</div>
                </div>
            </div>
        </div>

        <!-- ===== Product Detail Table ===== -->
        <div class="card p-3 mb-4">
            <h5 class="mb-3">Product Details</h5>
            <table class="table table-hover align-middle">
                <thead class="table-light">
                <tr>
                    <th>Product</th>
                    <th class="text-end">Qty Sold</th>
                    <th class="text-end">Paid Qty</th>
                    <th class="text-end">Paid Amount</th>
                    <th class="text-end">Unpaid Qty</th>
                    <th class="text-end">Outstanding Amount</th>
                </tr>
                </thead>
                <tbody id="productSummaryBody">
                </tbody>
            </table>
        </div>
    </div>

    <!-- ===== Bill List Section ===== -->
    <div id="billListSection">

        <!-- ===== Search / Filter Form ===== -->
        <div class="card p-3 mb-4">
            <form method="get" action="${pageContext.request.contextPath}/BillServlet" class="row g-2 align-items-end" id="billSearchForm">
                <input type="hidden" name="action" value="list">
                <input type="hidden" id="hf_chartMode" name="chartMode" value="${param.chartMode}">
                <input type="hidden" id="hf_chartPeriodType" name="chartPeriodType" value="${param.chartPeriodType}">
                <input type="hidden" id="hf_chartFromDate" name="chartFromDate" value="${param.chartFromDate}">
                <input type="hidden" id="hf_chartToDate" name="chartToDate" value="${param.chartToDate}">
                <input type="hidden" id="hf_productPeriodType" name="productPeriodType" value="${param.productPeriodType}">
                <input type="hidden" id="hf_productFilterId" name="productFilterId" value="${param.productFilterId}">
                <input type="hidden" id="hf_productFromDate" name="productFromDate" value="${param.productFromDate}">
                <input type="hidden" id="hf_productToDate" name="productToDate" value="${param.productToDate}">

                <div class="col-md-3">
                    <label class="form-label">Keyword</label>
                    <input type="text" name="keyword" value="${keyword}" class="form-control"
                           placeholder="Bill ID, Order ID, Customer name, Phone...">
                </div>

                <div class="col-md-2">
                    <label class="form-label">Payment Status</label>
                    <select name="paymentStatus" class="form-select">
                        <option value="">-- All --</option>
                        <c:forEach var="s" items="${['Pending','Paid','Failed','Refunded']}">
                            <option value="${s}" ${paymentStatus == s ? 'selected' : ''}>${s}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-2">
                    <label class="form-label">Order Status</label>
                    <select name="orderStatus" class="form-select">
                        <option value="">-- All --</option>
                        <c:forEach var="s" items="${['Pending','Confirmed','Processing','Shipping','Delivered','Cancelled']}">
                            <option value="${s}" ${orderStatus == s ? 'selected' : ''}>${s}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-2">
                    <label class="form-label">From Date</label>
                    <input type="date" name="fromDate" value="${fromDate}" class="form-control">
                </div>

                <div class="col-md-2">
                    <label class="form-label">To Date</label>
                    <input type="date" name="toDate" value="${toDate}" class="form-control">
                </div>

                <div class="col-md-1 d-grid">
                    <button type="submit" class="btn btn-primary">Filter</button>
                </div>
            </form>
        </div>

        <!-- ===== Bill List ===== -->
        <div class="card p-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h5 class="mb-0">Bill List (${totalBills} results)</h5>
                <div class="fw-bold">
                    Total: <fmt:formatNumber value="${totalOfList}" type="number" groupingUsed="true"/> VND
                </div>
            </div>

            <table class="table table-hover align-middle">
                <thead class="table-light">
                <tr>
                    <th>Bill ID</th>
                    <th>Order ID</th>
                    <th>Customer</th>
                    <th>Phone</th>
                    <th>Payment Method</th>
                    <th>Payment Status</th>
                    <th>Order Status</th>
                    <th>Issued Date</th>
                    <th>Total</th>
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
                        <td><fmt:formatNumber value="${b.totalAmount}" type="number" groupingUsed="true"/> VND</td>
                        <td>
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/BillServlet?action=detail&billId=${b.billId}">
                                View Details
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty bills}">
                    <tr><td colspan="10" class="text-center text-muted">No bills found.</td></tr>
                </c:if>
                </tbody>
            </table>

            <c:if test="${totalPages > 1}">
                <div class="d-flex justify-content-center mt-3">
                    <nav>
                        <ul class="pagination mb-0">
                            <c:if test="${currentPage > 1}">
                                <li class="page-item">
                                    <a class="page-link" href="?action=list&page=${currentPage - 1}&keyword=${keyword}&paymentStatus=${paymentStatus}&orderStatus=${orderStatus}&fromDate=${fromDate}&toDate=${toDate}">‹</a>
                                </li>
                            </c:if>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <li class="page-item active"><span class="page-link">${i}</span></li>
                                    </c:when>
                                    <c:when test="${i <= 3 || i > totalPages - 3 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                        <li class="page-item"><a class="page-link" href="?action=list&page=${i}&keyword=${keyword}&paymentStatus=${paymentStatus}&orderStatus=${orderStatus}&fromDate=${fromDate}&toDate=${toDate}">${i}</a></li>
                                    </c:when>
                                    <c:when test="${i == 4 && currentPage > 5}">
                                        <li class="page-item disabled"><span class="page-link">...</span></li>
                                    </c:when>
                                    <c:when test="${i == totalPages - 3 && currentPage < totalPages - 4}">
                                        <li class="page-item disabled"><span class="page-link">...</span></li>
                                    </c:when>
                                </c:choose>
                            </c:forEach>
                            <c:if test="${currentPage < totalPages}">
                                <li class="page-item">
                                    <a class="page-link" href="?action=list&page=${currentPage + 1}&keyword=${keyword}&paymentStatus=${paymentStatus}&orderStatus=${orderStatus}&fromDate=${fromDate}&toDate=${toDate}">›</a>
                                </li>
                            </c:if>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>

    </div> <!-- /#billListSection -->
</div>

<script>
let revenueChart = null;
// NOTE: Chart rendering code removed - canvas removed from HTML

const contextPath = '${pageContext.request.contextPath}';

// ================= Product Dropdown =================
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
        .catch(err => console.error('Product list load error:', err));
}

// ================= Product Summary (Table Only) =================
function loadProductData() {
    const productId = document.getElementById('productFilterSelect').value;
    const fromDate = document.getElementById('productFromDate').value;
    const toDate = document.getElementById('productToDate').value;

    const summaryParams = new URLSearchParams({ action: 'productSummary' });
    if (productId) summaryParams.append('productId', productId);
    if (fromDate) summaryParams.append('fromDate', fromDate);
    if (toDate) summaryParams.append('toDate', toDate);

    fetch(contextPath + '/BillServlet?' + summaryParams.toString())
        .then(res => res.json())
        .then(data => {
            document.getElementById('sumTotalQuantity').textContent = Number(data.totalQuantity).toLocaleString('vi-VN');
            document.getElementById('sumTotalPaid').textContent = Number(data.totalPaidAmount).toLocaleString('vi-VN') + ' VND';
            document.getElementById('sumTotalUnpaid').textContent = Number(data.totalUnpaidAmount).toLocaleString('vi-VN') + ' VND';
            document.getElementById('sumPaidQtyCount').textContent = Number(data.totalPaidQuantity).toLocaleString('vi-VN');
            document.getElementById('sumUnpaidQtyCount').textContent = Number(data.totalUnpaidQuantity).toLocaleString('vi-VN');

            const tbody = document.getElementById('productSummaryBody');
            tbody.innerHTML = '';

            if (!data.rows || data.rows.length === 0) {
                const tr = document.createElement('tr');
                const td = document.createElement('td');
                td.colSpan = 6;
                td.className = 'text-center text-muted';
                td.textContent = 'No data found.';
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
                tdPaid.textContent = Number(r.totalRevenuePaid).toLocaleString('vi-VN') + ' VND';

                const tdUnpaid = document.createElement('td');
                tdUnpaid.className = 'text-end';
                tdUnpaid.textContent = Number(r.totalUnpaidAmount).toLocaleString('vi-VN') + ' VND';

                tr.append(tdName, tdQty, tdPaidQty, tdPaid, tdUnpaidQty, tdUnpaid);
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Product summary load error:', err));
}

// ================= View Mode Toggle =================
function switchMode(mode) {
    document.getElementById('revenuePanel').style.display = mode === 'revenue' ? '' : 'none';
    document.getElementById('productPanel').style.display = mode === 'product' ? '' : 'none';
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
document.getElementById('btnLoadChart').addEventListener('click', () => {});
document.getElementById('periodType').addEventListener('change', () => {});
document.getElementById('btnLoadProduct').addEventListener('click', loadProductData);

document.getElementById('billSearchForm').addEventListener('submit', function () {
    document.getElementById('hf_chartPeriodType').value = document.getElementById('periodType').value;
    document.getElementById('hf_chartFromDate').value = document.getElementById('chartFromDate').value;
    document.getElementById('hf_chartToDate').value = document.getElementById('chartToDate').value;
    document.getElementById('hf_productPeriodType').value = document.getElementById('productPeriodType').value;
    document.getElementById('hf_productFilterId').value = document.getElementById('productFilterSelect').value;
    document.getElementById('hf_productFromDate').value = document.getElementById('productFromDate').value;
    document.getElementById('hf_productToDate').value = document.getElementById('productToDate').value;
});

// ================= Initialize =================
const initialMode = '${empty param.chartMode ? "revenue" : param.chartMode}';

document.getElementById('revenuePanel').style.display = initialMode === 'revenue' ? '' : 'none';
document.getElementById('productPanel').style.display = initialMode === 'product' ? '' : 'none';
document.getElementById('billListSection').style.display = initialMode === 'revenue' ? '' : 'none';
document.getElementById('btnModeRevenue').classList.toggle('active', initialMode === 'revenue');
document.getElementById('btnModeProduct').classList.toggle('active', initialMode === 'product');
document.getElementById('hf_chartMode').value = initialMode;

loadProductOptions().then(() => {
    if (initialMode === 'product') {
        loadProductData();
    }
});
</script>
</body>
</html>
