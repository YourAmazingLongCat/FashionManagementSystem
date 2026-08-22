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
            transition: 0.3s; font-weight: 500; display: flex; align-items: center;
            text-decoration: none;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: #34495e; color: #fff; border-left-color: #1abc9c;
        }
        .sidebar .nav-link i { width: 24px; margin-right: 10px; }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); padding: 0; margin: 0; list-style: none; }
        .sidebar .nav-item { list-style: none; }
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
        .status-badge {
            display: inline-block; padding: 6px 12px; border-radius: 999px;
            font-weight: 600; font-size: 0.85rem;
        }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-processing, .status-confirmed, .status-shipping { background: #cffafe; color: #155e75; }
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
            <div class="brand"><i class="fas fa-user-cog"></i> Staff</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link ${param.currentPage == null || param.currentPage == 'orders' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/staff/orders">
                        <i class="fas fa-shopping-cart"></i> Manage Orders
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.currentPage == 'payments' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/staff/payments">
                        <i class="fas fa-credit-card"></i> Manage Payments
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.currentPage == 'products' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/staff/products">
                        <i class="fas fa-boxes"></i> Manage Products
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.currentPage == 'manageVariants' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/staff/products?action=manageVariants">
                        <i class="fas fa-tags"></i> Manage Variants
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.currentPage == 'warehouse' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <i class="fas fa-warehouse"></i> Manage Warehouse
                    </a>
                </li>
                <li class="nav-item mt-auto">
                    <a class="nav-link" href="${pageContext.request.contextPath}/profile">
                        <i class="fas fa-user"></i> Profile
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/home">
                        <i class="fas fa-home"></i> Back to Home
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">
                        <i class="fas fa-sign-out-alt"></i> Logout
                    </a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">

            <c:set var="currentPage" value="${param.currentPage}" />
            <c:if test="${empty currentPage}">
                <c:set var="currentPage" value="orders" />
            </c:if>

            <!-- ==================== ORDERS ==================== -->
            <div id="orders" class="section-card ${currentPage != 'orders' ? 'hidden-section' : ''}">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4 class="mb-0">Orders</h4>
                    <small class="text-muted">${empty totalOrders ? 0 : totalOrders} total</small>
                </div>

                <div class="row mb-4 g-3">
                    <div class="col-md-3 col-6"><div class="stat-card"><div class="stat-label"><i class="fas fa-receipt"></i> Total</div><div class="stat-number">${empty totalOrders ? 0 : totalOrders}</div></div></div>
                    <div class="col-md-3 col-6"><div class="stat-card" style="border-left-color: #f59e0b;"><div class="stat-label"><i class="fas fa-clock"></i> Pending</div><div class="stat-number">${empty pendingOrders ? 0 : pendingOrders}</div></div></div>
                    <div class="col-md-3 col-6"><div class="stat-card" style="border-left-color: #3498db;"><div class="stat-label"><i class="fas fa-cogs"></i> Processing</div><div class="stat-number">${empty processingOrders ? 0 : processingOrders}</div></div></div>
                    <div class="col-md-3 col-6"><div class="stat-card" style="border-left-color: #27ae60;"><div class="stat-label"><i class="fas fa-truck"></i> Shipping</div><div class="stat-number">${empty shippingOrders ? 0 : shippingOrders}</div></div></div>
                </div>

                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-shopping-cart me-2"></i> Recent orders</span>
                        <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-sm btn-outline-secondary">Open full page</a>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Order ID</th>
                                        <th>Customer</th>
                                        <th>Phone</th>
                                        <th>Status</th>
                                        <th class="text-end">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty recentOrders}">
                                            <c:forEach var="order" items="${recentOrders}" varStatus="loop">
                                                <c:if test="${loop.index < 10}">
                                                    <tr>
                                                        <td><code>${order.orderId}</code></td>
                                                        <td>${order.customerId}</td>
                                                        <td>${order.phone}</td>
                                                        <td><span class="status-badge status-${fn:toLowerCase(order.orderStatus)}">${order.orderStatus}</span></td>
                                                        <td class="text-end"><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ</td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="5" class="text-center text-muted py-4">No order data available.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==================== PAYMENTS ==================== -->
            <div id="payments" class="section-card ${currentPage != 'payments' ? 'hidden-section' : ''}">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4 class="mb-0">Payments</h4>
                    <small class="text-muted">${empty totalPayments ? 0 : totalPayments} total</small>
                </div>

                <div class="row mb-4 g-3">
                    <div class="col-md-3 col-6"><div class="stat-card"><div class="stat-label"><i class="fas fa-money-bill"></i> Total</div><div class="stat-number">${empty totalPayments ? 0 : totalPayments}</div></div></div>
                    <div class="col-md-3 col-6"><div class="stat-card" style="border-left-color: #f59e0b;"><div class="stat-label"><i class="fas fa-clock"></i> Pending</div><div class="stat-number">${empty pendingPayments ? 0 : pendingPayments}</div></div></div>
                    <div class="col-md-3 col-6"><div class="stat-card" style="border-left-color: #27ae60;"><div class="stat-label"><i class="fas fa-check-circle"></i> Paid</div><div class="stat-number">${empty paidPayments ? 0 : paidPayments}</div></div></div>
                    <div class="col-md-3 col-6"><div class="stat-card" style="border-left-color: #e74c3c;"><div class="stat-label"><i class="fas fa-circle-xmark"></i> Failed</div><div class="stat-number">${empty failedPayments ? 0 : failedPayments}</div></div></div>
                </div>

                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-credit-card me-2"></i> Recent payments</span>
                        <a href="${pageContext.request.contextPath}/staff/payments" class="btn btn-sm btn-outline-secondary">Open full page</a>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Payment ID</th>
                                        <th>Type</th>
                                        <th>Method</th>
                                        <th>Status</th>
                                        <th class="text-end">Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty payments}">
                                            <c:forEach var="payment" items="${payments}" varStatus="loop">
                                                <c:if test="${loop.index < 10}">
                                                    <tr>
                                                        <td><code>${payment.paymentId}</code></td>
                                                        <td>${payment.paymentType}</td>
                                                        <td>${payment.paymentMethod}</td>
                                                        <td>${payment.paymentStatus}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> đ</td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="5" class="text-center text-muted py-4">No payment data available.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==================== PRODUCTS ==================== -->
            <div id="products" class="section-card ${currentPage != 'products' ? 'hidden-section' : ''}">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4 class="mb-0">Products</h4>
                    <a href="${pageContext.request.contextPath}/staff/products" class="btn btn-sm btn-outline-secondary">Open product page</a>
                </div>

                <div class="row mb-4 g-3">
                    <div class="col-md-4"><div class="stat-card"><div class="stat-label"><i class="fas fa-box"></i> Total products</div><div class="stat-number">${empty totalProducts ? 0 : totalProducts}</div></div></div>
                    <div class="col-md-4"><div class="stat-card" style="border-left-color: #3498db;"><div class="stat-label"><i class="fas fa-chart-line"></i> Total sold</div><div class="stat-number">${empty totalProductSold ? 0 : totalProductSold}</div></div></div>
                    <div class="col-md-4"><div class="stat-card" style="border-left-color: #27ae60;"><div class="stat-label"><i class="fas fa-dollar-sign"></i> Revenue</div><div class="stat-number"><fmt:formatNumber value="${empty revenue ? 0 : revenue}" pattern="#,##0" /> đ</div></div></div>
                </div>
            </div>

            <!-- ==================== WAREHOUSE ==================== -->
            <div id="warehouse" class="section-card ${currentPage != 'warehouse' ? 'hidden-section' : ''}">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4 class="mb-0">Warehouse</h4>
                    <a href="${pageContext.request.contextPath}/staff/warehouse/inventory" class="btn btn-sm btn-outline-secondary">Open warehouse page</a>
                </div>
                <div class="card">
                    <div class="card-body text-center text-muted py-5">
                        <i class="fas fa-warehouse fa-2x mb-2 d-block"></i>
                        Inventory management.
                    </div>
                </div>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
