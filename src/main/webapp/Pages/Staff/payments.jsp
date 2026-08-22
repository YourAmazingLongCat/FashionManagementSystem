<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Management - Staff</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { background: #f8f9fa; }
        .sidebar {
            background: linear-gradient(180deg, #2c3e50, #1a252f);
            position: sticky; top: 0; height: 100vh; overflow-y: auto;
            align-self: flex-start; padding: 0; color: #ecf0f1;
        }
        .sidebar .brand {
            padding: 20px 15px; font-size: 1.5rem; font-weight: 600;
            border-bottom: 1px solid #34495e; text-align: center;
        }
        .sidebar .nav-link {
            color: #b0c4de; padding: 12px 20px; border-left: 3px solid transparent;
            transition: 0.3s; font-weight: 500;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: #34495e; color: #fff; border-left-color: #1abc9c;
        }
        .sidebar .nav-link i { width: 24px; margin-right: 10px; }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); }
        .sidebar .nav-item.mt-auto { margin-top: auto; }
        .main-content { padding: 20px 30px; }
        .stat-card {
            background: #fff; border-radius: 12px; padding: 20px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-left: 4px solid #1abc9c;
            transition: 0.2s;
        }
        .stat-card:hover { transform: translateY(-4px); }
        .stat-card .stat-number { font-size: 2rem; font-weight: 700; }
        .stat-card .stat-label { color: #6c757d; text-transform: uppercase; font-size: 0.9rem; }
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .card-header { background: #f8f9fa; font-weight: 600; }
        .table th { background: #f1f3f5; border-top: none; }
        .status-badge {
            display: inline-block; padding: 6px 12px; border-radius: 999px;
            font-weight: 600; font-size: 0.85rem;
        }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-paid { background: #dcfce7; color: #166534; }
        .status-failed { background: #fee2e2; color: #991b1b; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        .status-refunded { background: #e0e7ff; color: #3730a3; }
        @media (max-width: 768px) {
            .sidebar { min-height: auto; height: auto; }
            .main-content { padding: 15px; }
            .stat-card .stat-number { font-size: 1.5rem; }
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand">Staff</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Manage Orders</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/payments">Manage Payments</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">Manage Products</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Manage Variants</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Manage Warehouse</a>
                </li>
                <li class="nav-item mt-auto">
                    <a class="nav-link" href="${pageContext.request.contextPath}/profile">Profile</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">Logout</a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">

            <c:set var="pagePaidPayments" value="${0}" />
            <c:set var="pagePendingPayments" value="${0}" />
            <c:set var="pageFailedPayments" value="${0}" />
            <c:forEach var="payment" items="${payments}">
                <c:choose>
                    <c:when test="${payment.paymentStatus eq 'Paid'}">
                        <c:set var="pagePaidPayments" value="${pagePaidPayments + 1}" />
                    </c:when>
                    <c:when test="${payment.paymentStatus eq 'Pending'}">
                        <c:set var="pagePendingPayments" value="${pagePendingPayments + 1}" />
                    </c:when>
                    <c:when test="${payment.paymentStatus eq 'Failed'}">
                        <c:set var="pageFailedPayments" value="${pageFailedPayments + 1}" />
                    </c:when>
                </c:choose>
            </c:forEach>

            <!-- Top stat cards -->
            <div class="row mb-4 g-3">
                <div class="col-md-3 col-6">
                    <div class="stat-card">
                        <div class="stat-label">Total Records</div>
                        <div class="stat-number">${empty totalPayments ? fn:length(payments) : totalPayments}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #f59e0b;">
                        <div class="stat-label">Pending</div>
                        <div class="stat-number">${pagePendingPayments}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #27ae60;">
                        <div class="stat-label">Paid</div>
                        <div class="stat-number">${pagePaidPayments}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #e74c3c;">
                        <div class="stat-label">Failed</div>
                        <div class="stat-number">${pageFailedPayments}</div>
                    </div>
                </div>
            </div>

            <!-- Payment records table -->
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center flex-wrap">
                    <span>Payment Records</span>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty payments}">
                            <div class="text-center text-muted py-5">
                                <p class="mb-0">No payment records found.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped align-middle">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Payment ID</th>
                                            <th>Type</th>
                                            <th>Method</th>
                                            <th>Status</th>
                                            <th class="text-end">Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="payment" items="${payments}" varStatus="loop">
                                            <tr>
                                                <td>${loop.index + 1}</td>
                                                <td><code>${payment.paymentId}</code></td>
                                                <td>${payment.paymentType}</td>
                                                <td>${payment.paymentMethod}</td>
                                                <td><span class="status-badge status-${fn:toLowerCase(payment.paymentStatus)}">${payment.paymentStatus}</span></td>
                                                <td class="text-end fw-semibold"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> đ</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty totalPages and totalPages > 1}">
                        <nav class="mt-3">
                            <ul class="pagination justify-content-center pagination-sm flex-wrap">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}">Prev</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page=${i}&pageSize=${pageSize}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}">Next</a>
                                </li>
                            </ul>
                            <div class="text-center text-muted">Page ${currentPage} of ${totalPages}</div>
                        </nav>
                    </c:if>
                </div>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
