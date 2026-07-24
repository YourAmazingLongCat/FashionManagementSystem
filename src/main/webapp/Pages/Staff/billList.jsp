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

    <!-- ===== Bill List Section ===== -->
    <div id="billListSection">

        <!-- ===== Search / Filter Form ===== -->
        <div class="card p-3 mb-4">
            <form method="get" action="${pageContext.request.contextPath}/BillServlet" class="row g-2 align-items-end" id="billSearchForm">
                <input type="hidden" name="action" value="list">

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
                    <tr><td colspan="9" class="text-center text-muted">No bills found.</td></tr>
                </c:if>
                </tbody>
            </table>

            <c:if test="${totalPages > 1}">
                <div class="d-flex justify-content-center mt-3">
                    <nav>
                        <ul class="pagination mb-0">
                            <c:if test="${currentPage > 1}">
                                <li class="page-item">
                                    <a class="page-link" href="?action=list&page=${currentPage - 1}&keyword=${keyword}&paymentStatus=${paymentStatus}&orderStatus=${orderStatus}">&#8249;</a>
                                </li>
                            </c:if>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <li class="page-item active"><span class="page-link">${i}</span></li>
                                    </c:when>
                                    <c:when test="${i <= 3 || i > totalPages - 3 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                        <li class="page-item"><a class="page-link" href="?action=list&page=${i}&keyword=${keyword}&paymentStatus=${paymentStatus}&orderStatus=${orderStatus}">${i}</a></li>
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
                                    <a class="page-link" href="?action=list&page=${currentPage + 1}&keyword=${keyword}&paymentStatus=${paymentStatus}&orderStatus=${orderStatus}">&#8250;</a>
                                </li>
                            </c:if>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>

    </div> <!-- /#billListSection -->
</div>
</body>
</html>
