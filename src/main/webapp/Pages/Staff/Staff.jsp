<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { background: #f8f9fa; }
        .sidebar {
            background: linear-gradient(180deg, #243b53, #102a43);
            min-height: 100vh; padding: 0; color: #ecf0f1;
        }
        .sidebar .brand {
            padding: 20px 15px; font-size: 1.5rem; font-weight: 600;
            border-bottom: 1px solid rgba(255,255,255,0.12); text-align: center;
        }
        .sidebar .nav-link {
            color: #c9d6df; padding: 12px 20px; border-left: 3px solid transparent;
            transition: 0.3s; font-weight: 500;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: rgba(255,255,255,0.08); color: #fff; border-left-color: #38bdf8;
        }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); }
        .sidebar .nav-link i { width: 24px; margin-right: 10px; }
        .sidebar .nav-item.mt-auto { margin-top: auto; }
        .sidebar .nav-link.back-home {
            margin-top: 30px; border-top: 1px solid rgba(255,255,255,0.12); padding-top: 20px;
            color: #f8d66d;
        }
        .sidebar .nav-link.back-home:hover { background: transparent; color: #ffd166; }
        .main-content { padding: 20px 30px; }
        .stat-card {
            background: #fff; border-radius: 12px; padding: 20px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-left: 4px solid #38bdf8;
            transition: 0.2s; height: 100%;
        }
        .stat-card:hover { transform: translateY(-4px); }
        .stat-card .stat-number { font-size: 2rem; font-weight: 700; }
        .stat-card .stat-label { color: #6c757d; text-transform: uppercase; font-size: 0.9rem; }
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: none; }
        .card-header { background: #f8f9fa; font-weight: 600; border-bottom: 1px solid #eef1f4; }
        .table th { background: #f1f3f5; border-top: none; }
        .hidden-section { display: none; }
        .quick-link-card {
            border: 1px dashed #cbd5e1; border-radius: 12px; padding: 18px; background: #fff; height: 100%;
        }
        .quick-link-card h6 { font-weight: 700; margin-bottom: 8px; }
        .quick-link-card p { color: #6b7280; margin-bottom: 14px; }
        .section-empty {
            text-align: center; padding: 40px 20px; color: #6b7280;
        }
        .section-empty i { font-size: 2rem; margin-bottom: 12px; color: #94a3b8; }
        .badge-soft {
            background: #e0f2fe; color: #0369a1; font-weight: 600; padding: 0.45rem 0.75rem; border-radius: 999px;
        }
        .badge-soft-warning { background: #fef3c7; color: #92400e; }
        .badge-soft-success { background: #dcfce7; color: #166534; }
        .action-buttons { display: flex; flex-wrap: wrap; gap: 10px; }
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
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand"><i class="fas fa-user-cog"></i> Staff</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link ${empty param.section || param.section == 'overview' ? 'active' : ''}" href="?section=overview">
                        <i class="fas fa-gauge-high"></i> Overview
                    </a>
                </li>
                <c:if test="${sessionScope.USER.role ne 'Admin'}">
                    <li class="nav-item">
                        <a class="nav-link ${param.section == 'profile' ? 'active' : ''}" href="${pageContext.request.contextPath}/profile">
                            <i class="fas fa-user"></i> Profile
                        </a>
                    </li>
                </c:if>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'orderManagement' ? 'active' : ''}" href="?section=orderManagement">
                        <i class="fas fa-shopping-cart"></i> Order Management
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'paymentManagement' ? 'active' : ''}" href="?section=paymentManagement">
                        <i class="fas fa-wallet"></i> Payment Management
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'productManagement' ? 'active' : ''}" href="?section=productManagement">
                        <i class="fas fa-box-open"></i> Product Management
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'warehouseManagement' ? 'active' : ''}" href="?section=warehouseManagement">
                        <i class="fas fa-warehouse"></i> Warehouse Management
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'customerSupport' ? 'active' : ''}" href="?section=customerSupport">
                        <i class="fas fa-headset"></i> Customer Support
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link back-home" href="${pageContext.request.contextPath}/home">
                        <i class="fas fa-home"></i> Back to Home
                    </a>
                </li>
                <c:if test="${sessionScope.USER.role eq 'Admin'}">
                    <li class="nav-item mt-auto">
                        <a class="nav-link ${param.section == 'profile' ? 'active' : ''}" href="${pageContext.request.contextPath}/profile">
                            <i class="fas fa-user"></i> Profile
                        </a>
                    </li>
                </c:if>
                <c:if test="${sessionScope.USER.role eq 'Admin' || sessionScope.USER.role eq 'Staff'}">
                    <li class="nav-item">
                        <a class="nav-link back-home" href="${pageContext.request.contextPath}/auth/logout">
                            <i class="fas fa-sign-out-alt"></i> Logout
                        </a>
                    </li>
                </c:if>
            </ul>
        </div>

        <div class="col-md-9 col-lg-10 main-content">
            <div class="row mb-4 g-3">
                <div class="col-md-3 col-6">
                    <div class="stat-card">
                        <div class="stat-label"><i class="fas fa-receipt"></i> Total Orders</div>
                        <div class="stat-number">${totalOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #f59e0b;">
                        <div class="stat-label"><i class="fas fa-clock"></i> Pending Orders</div>
                        <div class="stat-number">${pendingOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #8b5cf6;">
                        <div class="stat-label"><i class="fas fa-money-bill-transfer"></i> Pending Deposits</div>
                        <div class="stat-number">${pendingDepositsCount}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #10b981;">
                        <div class="stat-label"><i class="fas fa-boxes-stacked"></i> Products</div>
                        <div class="stat-number">${totalProducts}</div>
                    </div>
                </div>
            </div>

            <c:set var="currentSection" value="${param.section}" />
            <c:if test="${empty currentSection}">
                <c:set var="currentSection" value="overview" />
            </c:if>

            <div class="section-card ${currentSection != 'overview' ? 'hidden-section' : ''}">
                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-gauge-high me-2"></i> Staff Overview</span>
                        <span class="badge-soft">Operations Centre</span>
                    </div>
                    <div class="card-body">
                        <div class="row g-3">
                            <div class="col-md-6 col-xl-3">
                                <div class="quick-link-card">
                                    <h6>Order Management</h6>
                                    <p>Review, confirm, cancel and track shipping status for customer orders.</p>
                                    <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-primary btn-sm">Open Orders</a>
                                </div>
                            </div>
                            <div class="col-md-6 col-xl-3">
                                <div class="quick-link-card">
                                    <h6>Payment Management</h6>
                                    <p>Handle deposit approvals, payment records and order payment monitoring.</p>
                                    <a href="${pageContext.request.contextPath}/staff/payments" class="btn btn-primary btn-sm">Open Payments</a>
                                </div>
                            </div>
                            <div class="col-md-6 col-xl-3">
                                <div class="quick-link-card">
                                    <h6>Product Management</h6>
                                    <p>Manage product catalogue and variants in the existing staff module.</p>
                                    <a href="${pageContext.request.contextPath}/staff/products" class="btn btn-outline-secondary btn-sm">Open Products</a>
                                </div>
                            </div>
                            <div class="col-md-6 col-xl-3">
                                <div class="quick-link-card">
                                    <h6>More Tools</h6>
                                    <p>Warehouse, support and other tools will be placed here in the next phase.</p>
                                    <a href="?section=warehouseManagement" class="btn btn-outline-secondary btn-sm">View Placeholder</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-lg-7">
                        <div class="card h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <span><i class="fas fa-truck-fast me-2"></i> Recent Orders</span>
                                <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-sm btn-outline-primary">View All</a>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-hover align-middle">
                                        <thead>
                                            <tr>
                                                <th>Order ID</th>
                                                <th>Customer</th>
                                                <th>Status</th>
                                                <th>Total</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty recentOrders}">
                                                    <c:forEach var="order" items="${recentOrders}" varStatus="loop">
                                                        <c:if test="${loop.index < 5}">
                                                            <tr>
                                                                <td>${order.orderId}</td>
                                                                <td>${order.customerId}</td>
                                                                <td>
                                                                    <span class="badge ${order.orderStatus == 'Pending' ? 'bg-warning text-dark' : order.orderStatus == 'Delivered' ? 'bg-success' : order.orderStatus == 'Cancelled' ? 'bg-danger' : 'bg-primary'}">
                                                                        ${order.orderStatus}
                                                                    </span>
                                                                </td>
                                                                <td><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ</td>
                                                            </tr>
                                                        </c:if>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <tr><td colspan="4" class="text-center text-muted">No recent orders</td></tr>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-5">
                        <div class="card h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <span><i class="fas fa-money-check-dollar me-2"></i> Payment Snapshot</span>
                                <span class="badge-soft-warning">${pendingDepositsCount} pending deposits</span>
                            </div>
                            <div class="card-body">
                                <ul class="list-group list-group-flush mb-3">
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        Total payment records
                                        <span class="badge bg-dark">${totalPayments}</span>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        Pending payments
                                        <span class="badge bg-warning text-dark">${pendingPayments}</span>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        Paid payments
                                        <span class="badge bg-success">${paidPayments}</span>
                                    </li>
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        Shipping orders
                                        <span class="badge bg-info text-dark">${shippingOrders}</span>
                                    </li>
                                </ul>
                                <div class="action-buttons">
                                    <a href="${pageContext.request.contextPath}/staff/payments" class="btn btn-primary btn-sm">Go to Payment Management</a>
                                    <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-outline-secondary btn-sm">Go to Order Management</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-card ${currentSection != 'orderManagement' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-shopping-cart me-2"></i> Order Management</span>
                        <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-primary btn-sm">Open Full Page</a>
                    </div>
                    <div class="card-body">
                        <div class="row mb-4 g-3">
                            <div class="col-md-4"><div class="quick-link-card"><h6>Total Orders</h6><p>All orders currently stored in the system.</p><div class="h3 mb-0">${totalOrders}</div></div></div>
                            <div class="col-md-4"><div class="quick-link-card"><h6>Pending Orders</h6><p>Orders waiting for staff confirmation.</p><div class="h3 mb-0">${pendingOrders}</div></div></div>
                            <div class="col-md-4"><div class="quick-link-card"><h6>Processing / Shipping</h6><p>Orders that are currently being fulfilled or delivered.</p><div class="h3 mb-0">${processingOrders + shippingOrders}</div></div></div>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover table-striped align-middle">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Order ID</th>
                                        <th>Customer</th>
                                        <th>Phone</th>
                                        <th>Status</th>
                                        <th>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty recentOrders}">
                                            <c:forEach var="order" items="${recentOrders}" varStatus="loop">
                                                <c:if test="${loop.index < 8}">
                                                    <tr>
                                                        <td>${loop.index + 1}</td>
                                                        <td>${order.orderId}</td>
                                                        <td>${order.customerId}</td>
                                                        <td>${order.phone}</td>
                                                        <td>${order.orderStatus}</td>
                                                        <td><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ</td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="6" class="text-center text-muted">No order data available.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-card ${currentSection != 'paymentManagement' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-wallet me-2"></i> Payment Management</span>
                        <a href="${pageContext.request.contextPath}/staff/payments" class="btn btn-primary btn-sm">Open Full Page</a>
                    </div>
                    <div class="card-body">
                        <div class="row mb-4 g-3">
                            <div class="col-md-4"><div class="quick-link-card"><h6>Total Payments</h6><p>All payment records for deposits and order purchases.</p><div class="h3 mb-0">${totalPayments}</div></div></div>
                            <div class="col-md-4"><div class="quick-link-card"><h6>Pending Payments</h6><p>Records that still require processing or confirmation.</p><div class="h3 mb-0">${pendingPayments}</div></div></div>
                            <div class="col-md-4"><div class="quick-link-card"><h6>Pending Deposits</h6><p>Wallet top-ups waiting for approval.</p><div class="h3 mb-0">${pendingDepositsCount}</div></div></div>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover table-striped align-middle">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Payment ID</th>
                                        <th>Type</th>
                                        <th>Method</th>
                                        <th>Status</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty payments}">
                                            <c:forEach var="payment" items="${payments}" varStatus="loop">
                                                <c:if test="${loop.index < 8}">
                                                    <tr>
                                                        <td>${loop.index + 1}</td>
                                                        <td>${payment.paymentId}</td>
                                                        <td>${payment.paymentType}</td>
                                                        <td>${payment.paymentMethod}</td>
                                                        <td>${payment.paymentStatus}</td>
                                                        <td><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> đ</td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="6" class="text-center text-muted">No payment data available.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-card ${currentSection != 'productManagement' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-box-open me-2"></i> Product Management</span>
                        <a href="${pageContext.request.contextPath}/staff/products" class="btn btn-outline-primary btn-sm">Open Existing Product Page</a>
                    </div>
                    <div class="card-body section-empty">
                        <i class="fas fa-screwdriver-wrench"></i>
                        <h5>This area is reserved for the integrated staff dashboard.</h5>
                        <p>The consolidated product management widgets can be added here later. For now, please use the dedicated product management page.</p>
                    </div>
                </div>
            </div>

            <div class="section-card ${currentSection != 'warehouseManagement' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-warehouse me-2"></i> Warehouse Management</div>
                    <div class="card-body section-empty">
                        <i class="fas fa-boxes-packing"></i>
                        <h5>Coming soon</h5>
                        <p>This section is intentionally left empty for future warehouse and inventory workflows.</p>
                    </div>
                </div>
            </div>

            <div class="section-card ${currentSection != 'customerSupport' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-headset me-2"></i> Customer Support</div>
                    <div class="card-body section-empty">
                        <i class="fas fa-comments"></i>
                        <h5>Coming soon</h5>
                        <p>This section is intentionally left empty for customer support, tickets and service management tools.</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
