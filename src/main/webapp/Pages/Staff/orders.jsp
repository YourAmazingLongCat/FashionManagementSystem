<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Management - Staff</title>
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
        .hidden-section { display: none; }
        .search-form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
        .search-form .form-control { width: auto; min-width: 220px; }
        .status-badge {
            display: inline-block; padding: 6px 12px; border-radius: 999px;
            font-weight: 600; font-size: 0.85rem;
        }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-processing { background: #dbeafe; color: #1e40af; }
        .status-confirmed { background: #e0e7ff; color: #3730a3; }
        .status-shipping { background: #cffafe; color: #155e75; }
        .status-delivered { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
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
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/orders">Orders</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/payments">Payments</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">Products</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Warehouse</a>
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

            <c:set var="pagePendingOrders" value="${0}" />
            <c:set var="pageShippingOrders" value="${0}" />
            <c:set var="pageDeliveredOrders" value="${0}" />
            <c:set var="pageProcessingOrders" value="${0}" />
            <c:forEach var="order" items="${listOrders}">
                <c:if test="${order.orderStatus eq 'Pending'}">
                    <c:set var="pagePendingOrders" value="${pagePendingOrders + 1}" />
                </c:if>
                <c:if test="${order.orderStatus eq 'Processing'}">
                    <c:set var="pageProcessingOrders" value="${pageProcessingOrders + 1}" />
                </c:if>
                <c:if test="${order.orderStatus eq 'Shipping'}">
                    <c:set var="pageShippingOrders" value="${pageShippingOrders + 1}" />
                </c:if>
                <c:if test="${order.orderStatus eq 'Delivered'}">
                    <c:set var="pageDeliveredOrders" value="${pageDeliveredOrders + 1}" />
                </c:if>
            </c:forEach>

            <!-- Top stat cards -->
            <div class="row mb-4 g-3">
                <div class="col-md-3 col-6">
                    <div class="stat-card">
                        <div class="stat-label">Total Records</div>
                        <div class="stat-number">${empty totalOrders ? fn:length(listOrders) : totalOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #f59e0b;">
                        <div class="stat-label">Pending</div>
                        <div class="stat-number">${pagePendingOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #3498db;">
                        <div class="stat-label">Shipping</div>
                        <div class="stat-number">${pageShippingOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #27ae60;">
                        <div class="stat-label">Delivered</div>
                        <div class="stat-number">${pageDeliveredOrders}</div>
                    </div>
                </div>
            </div>

            <!-- Order Records -->
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center flex-wrap">
                    <span>Order Records</span>
                </div>
                <div class="card-body">
                    <form class="search-form mb-3" method="get" action="${pageContext.request.contextPath}/staff/orders">
                        <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                               class="form-control" placeholder="Order ID, customer ID, phone, address, or status"/>
                        <select name="status" class="form-select" style="width: auto;">
                            <option value="">All Statuses</option>
                            <option value="Pending" ${status eq 'Pending' ? 'selected' : ''}>Pending</option>
                            <option value="Confirmed" ${status eq 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                            <option value="Processing" ${status eq 'Processing' ? 'selected' : ''}>Processing</option>
                            <option value="Shipping" ${status eq 'Shipping' ? 'selected' : ''}>Shipping</option>
                            <option value="Delivered" ${status eq 'Delivered' ? 'selected' : ''}>Delivered</option>
                            <option value="Cancelled" ${status eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                        </select>
                        <input type="date" name="dateFrom" value="${fn:escapeXml(dateFrom)}" class="form-control" style="width: auto;" title="From date"/>
                        <input type="date" name="dateTo" value="${fn:escapeXml(dateTo)}" class="form-control" style="width: auto;" title="To date"/>
                        <button type="submit" class="btn btn-primary">Apply</button>
                        <c:if test="${not empty keyword or not empty status or not empty dateFrom or not empty dateTo}">
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/orders">
                                Clear
                            </a>
                        </c:if>
                    </form>

                    <c:choose>
                        <c:when test="${empty listOrders}">
                            <div class="text-center text-muted py-5">
                                <p class="mb-0">No orders found. Try another search term.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped align-middle">
                                    <thead>
                                        <tr>
                                            <th>Order</th>
                                            <th>Customer</th>
                                            <th>Placed At</th>
                                            <th>Contact</th>
                                            <th>Status</th>
                                            <th class="text-end">Total</th>
                                            <th class="text-end">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="order" items="${listOrders}">
                                            <tr>
                                                <td>
                                                    <div class="fw-semibold"><code>${order.orderId}</code></div>
                                                    <small class="text-muted">${order.shippingAddress}</small>
                                                </td>
                                                <td>${order.customerId}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty order.placedAt}">
                                                            ${fn:replace(order.placedAt, 'T', ' ')}
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty order.phone}">${order.phone}</c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="status-badge status-${fn:toLowerCase(order.orderStatus)}">
                                                        ${order.orderStatus}
                                                    </span>
                                                </td>
                                                <td class="text-end fw-semibold">
                                                    <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ
                                                </td>
                                                <td class="text-end">
                                                    <a class="btn btn-sm btn-primary"
                                                       href="${pageContext.request.contextPath}/staff/order-detail?orderId=${order.orderId}">
                                                        Manage
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty totalPages and totalPages > 1}">
                        <nav class="mt-3">
                            <ul class="pagination justify-content-center flex-wrap pagination-sm">
                                <c:url var="previousPageUrl" value="/staff/orders">
                                    <c:param name="page" value="${currentPage - 1}" />
                                    <c:param name="pageSize" value="${pageSize}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="status" value="${status}" />
                                    <c:param name="dateFrom" value="${dateFrom}" />
                                    <c:param name="dateTo" value="${dateTo}" />
                                </c:url>
                                <c:url var="nextPageUrl" value="/staff/orders">
                                    <c:param name="page" value="${currentPage + 1}" />
                                    <c:param name="pageSize" value="${pageSize}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="status" value="${status}" />
                                    <c:param name="dateFrom" value="${dateFrom}" />
                                    <c:param name="dateTo" value="${dateTo}" />
                                </c:url>
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${previousPageUrl}">Prev</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page=${i}&pageSize=${pageSize}&keyword=${fn:escapeXml(keyword)}&status=${status}&dateFrom=${dateFrom}&dateTo=${dateTo}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${nextPageUrl}">Next</a>
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
