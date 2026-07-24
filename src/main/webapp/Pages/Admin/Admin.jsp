<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { background: #f8f9fa; }
        .sidebar {
            background: linear-gradient(180deg, #2c3e50, #1a252f);
            min-height: 100vh; padding: 0; color: #ecf0f1;
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
        .section-content { margin-top: 30px; }
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .card-header { background: #f8f9fa; font-weight: 600; }
        .table th { background: #f1f3f5; border-top: none; }
        .hidden-section { display: none; }
        .search-form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
        .search-form .form-control { width: auto; min-width: 180px; }
        .time-filter-bar {
            background: #fff; border-radius: 12px; padding: 15px 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04); margin-bottom: 20px;
            display: flex; flex-wrap: wrap; align-items: center; gap: 15px 20px;
        }
        .time-filter-bar .label { font-weight: 600; color: #2c3e50; }
        .time-filter-bar .form-control { border-radius: 8px; width: auto; min-width: 150px; }
        .time-filter-bar .btn-filter {
            background: #1abc9c; border: none; border-radius: 8px;
            padding: 6px 20px; color: #fff; font-weight: 600;
        }
        .time-filter-bar .btn-filter:hover { background: #16a085; }
        .rank-badge {
            display: inline-block; width: 28px; height: 28px; line-height: 28px;
            text-align: center; border-radius: 50%; font-weight: 700; font-size: 0.8rem;
            background: #e9ecef; color: #2c3e50;
        }
        .rank-badge.gold { background: #f1c40f; color: #fff; }
        .rank-badge.silver { background: #bdc3c7; color: #fff; }
        .rank-badge.bronze { background: #e67e22; color: #fff; }
        .profit-formula {
            background: #f8fafc; padding: 15px; border-radius: 8px;
            border-left: 4px solid #27ae60; margin-top: 10px;
        }
        .profit-formula .formula {
            font-family: 'Courier New', monospace; font-size: 1.1rem;
            font-weight: 600; color: #1a2634;
        }
        @media (max-width: 768px) {
            .sidebar { min-height: auto; height: auto; }
            .main-content { padding: 15px; }
            .stat-card .stat-number { font-size: 1.5rem; }
            .time-filter-bar { flex-direction: column; align-items: stretch; }
            .time-filter-bar .form-control { width: 100% !important; min-width: unset; }
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand"><i class="fas fa-chart-pie"></i> Admin</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link ${param.section == null || param.section == 'overview' ? 'active' : ''}" 
                       href="?section=overview">
                        <i class="fas fa-th-large"></i> Overview
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'customers' ? 'active' : ''}"
                       href="?section=customers">
                        <i class="fas fa-users"></i> Customers
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'accounts' ? 'active' : ''}"
                       href="?section=accounts">
                        <i class="fas fa-user-shield"></i> Accounts
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'products' ? 'active' : ''}" 
                       href="?section=products">
                        <i class="fas fa-boxes"></i> Products
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'orders' ? 'active' : ''}" 
                       href="?section=orders">
                        <i class="fas fa-shopping-cart"></i> Orders
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'profit' ? 'active' : ''}" 
                       href="?section=profit">
                        <i class="fas fa-chart-line"></i> Profit
                    </a>
                </li>
                <li class="nav-item mt-auto">
                    <a class="nav-link ${param.section == 'profile' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/profile">
                        <i class="fas fa-user"></i> Profile
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

            <!-- Time Filter Bar -->
            <div class="time-filter-bar">
                <span class="label"><i class="far fa-calendar-alt me-2"></i>Time period:</span>
                <form action="${pageContext.request.contextPath}/Admin" method="get" class="d-flex flex-wrap align-items-center gap-2">
                    <input type="date" name="fromDate" class="form-control" value="${param.fromDate}" placeholder="From">
                    <span class="text-muted">→</span>
                    <input type="date" name="toDate" class="form-control" value="${param.toDate}" placeholder="To">
                    <input type="hidden" name="section" value="${param.section != null ? param.section : 'overview'}">
                    <button type="submit" class="btn btn-filter"><i class="fas fa-filter me-1"></i> Filter</button>
                </form>
                <c:if test="${not empty param.fromDate or not empty param.toDate}">
                    <span class="badge bg-info text-dark">
                        <i class="far fa-clock me-1"></i>
                        <c:choose>
                            <c:when test="${not empty param.fromDate and not empty param.toDate}">${param.fromDate} → ${param.toDate}</c:when>
                            <c:when test="${not empty param.fromDate}">From ${param.fromDate}</c:when>
                            <c:when test="${not empty param.toDate}">Until ${param.toDate}</c:when>
                        </c:choose>
                    </span>
                </c:if>
            </div>

            <!-- Top summary cards -->
            <div class="row mb-4">
                <div class="col-md-3 col-6">
                    <div class="stat-card">
                        <div class="stat-label"><i class="fas fa-user"></i> Total Customers</div>
                        <div class="stat-number">${empty totalCustomers ? 0 : totalCustomers}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #3498db;">
                        <div class="stat-label"><i class="fas fa-receipt"></i> Total Orders</div>
                        <div class="stat-number">${empty totalOrders ? 0 : totalOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #e67e22;">
                        <div class="stat-label"><i class="fas fa-money-bill-wave"></i> Revenue</div>
                        <div class="stat-number">
                            <fmt:formatNumber value="${empty revenue ? 0 : revenue}" pattern="#,##0" /> ₫
                        </div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #27ae60;">
                        <div class="stat-label"><i class="fas fa-coins"></i> Profit</div>
                        <div class="stat-number">
                            <fmt:formatNumber value="${empty profit ? 0 : profit}" pattern="#,##0" /> ₫
                        </div>
                    </div>
                </div>
            </div>

            <!-- Determine current section -->
            <c:set var="currentSection" value="${param.section}" />
            <c:if test="${empty currentSection}">
                <c:set var="currentSection" value="overview" />
            </c:if>

            <!-- ==================== OVERVIEW ==================== -->
            <div id="overview" class="section-card ${currentSection != 'overview' ? 'hidden-section' : ''}">
                <div class="row">
                    <!-- Top Products -->
                    <div class="col-lg-6 mb-4">
                        <div class="card">
                            <div class="card-header"><i class="fas fa-fire me-2"></i> Top Selling Products</div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-hover table-striped">
                                        <thead>
                                            <tr><th>#</th><th>Product</th><th class="text-end">Qty Sold</th><th class="text-end">Revenue</th></tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty topProducts}">
                                                    <c:forEach var="p" items="${topProducts}" varStatus="loop" begin="0" end="4">
                                                        <tr>
                                                            <td>${loop.index + 1}</td>
                                                            <td>${p.productName}</td>
                                                            <td class="text-end fw-bold">${p.quantitySold}</td>
                                                            <td class="text-end"><fmt:formatNumber value="${p.revenue}" pattern="#,##0" /> ₫</td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise><tr><td colspan="4" class="text-center text-muted">No product data</td></tr></c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Top Spending Customers -->
                    <div class="col-lg-6 mb-4">
                        <div class="card">
                            <div class="card-header"><i class="fas fa-trophy me-2"></i> Top Spending Customers</div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-hover table-striped">
                                        <thead>
                                            <tr><th>#</th><th>Customer</th><th class="text-end">Orders</th><th class="text-end">Total Spent</th></tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty topSpenders}">
                                                    <c:forEach var="c" items="${topSpenders}" varStatus="loop" begin="0" end="4">
                                                        <tr>
                                                            <td>
                                                                <span class="rank-badge ${loop.index == 0 ? 'gold' : loop.index == 1 ? 'silver' : loop.index == 2 ? 'bronze' : ''}">${loop.index + 1}</span>
                                                            </td>
                                                            <td>${c.fullName}</td>
                                                            <td class="text-end">${c.totalOrders}</td>
                                                            <td class="text-end"><fmt:formatNumber value="${c.totalSpent}" pattern="#,##0" /> ₫</td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise><tr><td colspan="4" class="text-center text-muted">No customer data</td></tr></c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Profit Formula -->
                <div class="card mt-3">
                    <div class="card-header"><i class="fas fa-calculator me-2"></i> Profit Calculation (based on cost price)</div>
                    <div class="card-body">
                        <div class="profit-formula">
                            <div class="formula">
                                Profit = Revenue − Cost of Goods Sold
                            </div>
                            <div class="mt-2">
                                <strong>Revenue:</strong> <fmt:formatNumber value="${empty revenue ? 0 : revenue}" pattern="#,##0" /> ₫ &nbsp;|&nbsp;
                                <strong>Cost of Goods Sold:</strong> <fmt:formatNumber value="${empty costOfGoodsSold ? 0 : costOfGoodsSold}" pattern="#,##0" /> ₫ &nbsp;|&nbsp;
                                <strong>Net Profit:</strong> <span class="text-success"><fmt:formatNumber value="${empty profit ? 0 : profit}" pattern="#,##0" /> ₫</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==================== CUSTOMERS ==================== -->
            <div id="customers" class="section-card ${currentSection != 'customers' ? 'hidden-section' : ''}">
                <ul class="nav nav-tabs mb-3" id="customerTabs" role="tablist">
                    <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#byOrdersTab">By Orders</button></li>
                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#bySpendingTab">By Spending</button></li>
                </ul>
                <div class="tab-content">
                    <!-- By Orders -->
                    <div class="tab-pane fade show active" id="byOrdersTab">
                        <div class="card">
                            <div class="card-header d-flex justify-content-between align-items-center flex-wrap">
                                <span><i class="fas fa-users me-2"></i> Customers by Order Count</span>
                                <form action="${pageContext.request.contextPath}/Admin" method="post" class="search-form">
                                    <label for="minOrders" class="form-label mb-0">Min orders:</label>
                                    <input type="number" name="quantity" id="minOrders" class="form-control form-control-sm" value="${param.quantity != null ? param.quantity : 5}" min="1">
                                    <input type="hidden" name="section" value="customers">
                                    <button type="submit" class="btn btn-primary btn-sm"><i class="fas fa-search"></i> Filter</button>
                                </form>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-hover table-striped">
                                        <thead><tr><th>#</th><th>Full Name</th><th class="text-end">Total Orders</th></tr></thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty customerStatistics}">
                                                    <c:forEach var="c" items="${customerStatistics}" varStatus="loop">
                                                        <tr>
                                                            <td>${loop.index + 1}</td>
                                                            <td>${c.fullName}</td>
                                                            <td class="text-end fw-bold">${c.totalOrders}</td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise><tr><td colspan="3" class="text-center text-muted">No customers found</td></tr></c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- By Spending -->
                    <div class="tab-pane fade" id="bySpendingTab">
                        <div class="card">
                            <div class="card-header"><i class="fas fa-money-bill-alt me-2"></i> Customers by Total Spending</div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-hover table-striped">
                                        <thead><tr><th>#</th><th>Full Name</th><th class="text-end">Total Orders</th><th class="text-end">Total Spent</th></tr></thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty topSpenders}">
                                                    <c:forEach var="c" items="${topSpenders}" varStatus="loop">
                                                        <tr>
                                                            <td>${loop.index + 1}</td>
                                                            <td>${c.fullName}</td>
                                                            <td class="text-end">${c.totalOrders}</td>
                                                            <td class="text-end"><fmt:formatNumber value="${c.totalSpent}" pattern="#,##0" /> ₫</td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise><tr><td colspan="4" class="text-center text-muted">No spending data</td></tr></c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==================== ACCOUNTS ==================== -->
            <div id="accounts" class="section-card ${currentSection != 'accounts' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center flex-wrap">
                        <span><i class="fas fa-user-shield me-2"></i> Account Management</span>
                        <button class="btn btn-success btn-sm" data-bs-toggle="modal" data-bs-target="#createAccountModal">
                            <i class="fas fa-plus"></i> Create Staff
                        </button>
                    </div>
                    <div class="card-body">
                        <form method="get" action="${pageContext.request.contextPath}/Admin" class="mb-3">
                            <input type="hidden" name="section" value="accounts"/>
                            <div class="input-group">
                                <input type="text" name="searchAccount" class="form-control" placeholder="Search by email or phone..." value="${fn:escapeXml(param.searchAccount)}"/>
                                <button type="submit" class="btn btn-primary"><i class="fas fa-search"></i> Search</button>
                                <a href="${pageContext.request.contextPath}/Admin?section=accounts" class="btn btn-outline-secondary"><i class="fas fa-times"></i> Clear</a>
                            </div>
                        </form>
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Account ID</th>
                                        <th>Email</th>
                                        <th>Full Name</th>
                                        <th>Phone</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty pagedAccounts}">
                                            <c:forEach var="acc" items="${pagedAccounts}" varStatus="loop">
                                                <tr>
                                                    <td>${(accountPage - 1) * accountPageSize + loop.index + 1}</td>
                                                    <td><code>${acc.accountId}</code></td>
                                                    <td>${acc.email}</td>
                                                    <td>${acc.fullName}</td>
                                                    <td>${not empty acc.phone ? acc.phone : '-'}</td>
                                                    <td>
                                                        <span class="badge ${acc.role == 'Admin' ? 'bg-danger' : acc.role == 'Staff' ? 'bg-warning text-dark' : 'bg-secondary'}">
                                                            ${acc.role}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <c:if test="${acc.role != 'Admin'}">
                                                            <form action="${pageContext.request.contextPath}/Admin" method="post" class="d-inline">
                                                                <input type="hidden" name="section" value="accounts"/>
                                                                <input type="hidden" name="action" value="updateStatus"/>
                                                                <input type="hidden" name="accountId" value="${acc.accountId}"/>
                                                                <select name="status" class="form-select form-select-sm d-inline w-auto" onchange="this.form.submit()">
                                                                    <option value="Active" ${acc.status == 'Active' ? 'selected' : ''}>Active</option>
                                                                    <option value="Banned" ${acc.status == 'Banned' ? 'selected' : ''}>Banned</option>
                                                                </select>
                                                            </form>
                                                        </c:if>
                                                        <c:if test="${acc.role == 'Admin'}">
                                                            <span class="badge ${acc.status == 'Active' ? 'bg-success' : 'bg-danger'}">${acc.status}</span>
                                                        </c:if>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise><tr><td colspan="7" class="text-center text-muted">No accounts found</td></tr></c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                        <!-- Pagination -->
                        <c:if test="${accountTotalPages > 1}">
                            <nav aria-label="Account pagination">
                                <ul class="pagination justify-content-center flex-wrap mb-0 pagination-sm">
                                    <li class="page-item ${accountPage == 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="?section=accounts&page=${accountPage - 1}<c:if test="${not empty param.searchAccount}">&searchAccount=${fn:escapeXml(param.searchAccount)}</c:if>">
                                            <i class="fas fa-chevron-left"></i>
                                        </a>
                                    </li>
                                    <c:forEach var="i" begin="1" end="${accountTotalPages}">
                                        <li class="page-item ${i == accountPage ? 'active' : ''}">
                                            <a class="page-link" href="?section=accounts&page=${i}<c:if test="${not empty param.searchAccount}">&searchAccount=${fn:escapeXml(param.searchAccount)}</c:if>">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${accountPage == accountTotalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="?section=accounts&page=${accountPage + 1}<c:if test="${not empty param.searchAccount}">&searchAccount=${fn:escapeXml(param.searchAccount)}</c:if>">
                                            <i class="fas fa-chevron-right"></i>
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                            </c:if>
                    </div>
                </div>
            </div>

            <!-- ==================== PRODUCTS ==================== -->
            <div id="products" class="section-card ${currentSection != 'products' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-boxes me-2"></i> Product Sales – Quantity Sold</div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Product Code</th>
                                            <th>Product Name</th>
                                            <th class="text-end">Base Price</th>
                                            <th class="text-end">Avg Sell Price</th>
                                            <th class="text-end">Qty Sold</th>
                                            <th class="text-end">Revenue</th>
                                            <th class="text-end">Profit</th>
                                        </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty productSales}">
                                            <c:forEach var="p" items="${productSales}" varStatus="loop">
                                                <tr>
                                                    <td>${loop.index + 1}</td>
                                                    <td><code>${p.productCode}</code></td>
                                                    <td>${p.productName}</td>
                                                    <td class="text-end"><fmt:formatNumber value="${p.unitCost}" pattern="#,##0" /> ₫</td>
                                                    <td class="text-end"><fmt:formatNumber value="${p.unitPrice}" pattern="#,##0" /> ₫</td>
                                                    <td class="text-end fw-bold">${p.quantitySold}</td>
                                                    <td class="text-end"><fmt:formatNumber value="${p.revenue}" pattern="#,##0" /> ₫</td>
                                                    <td class="text-end text-success"><fmt:formatNumber value="${p.profit}" pattern="#,##0" /> ₫</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise><tr><td colspan="8" class="text-center text-muted">No product data</td></tr></c:otherwise>
                                    </c:choose>
                                </tbody>
                                <tfoot class="table-light fw-bold">
                                    <tr>
                                        <td colspan="5" class="text-end">TOTAL</td>
                                        <td class="text-end">${empty totalProductSold ? 0 : totalProductSold}</td>
                                        <td class="text-end"><fmt:formatNumber value="${empty revenue ? 0 : revenue}" pattern="#,##0" /> ₫</td>
                                        <td class="text-end text-success"><fmt:formatNumber value="${empty profit ? 0 : profit}" pattern="#,##0" /> ₫</td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==================== ORDERS ==================== -->
            <div id="orders" class="section-card ${currentSection != 'orders' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-shopping-cart me-2"></i> Order Statistics</div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead><tr><th>Status</th><th class="text-end">Quantity</th></tr></thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty orderStatistics}">
                                                    <c:forEach var="o" items="${orderStatistics}">
                                                        <tr>
                                                            <td><span class="badge bg-${o.status == 'Delivered' ? 'success' : o.status == 'Pending' || o.status == 'Processing' || o.status == 'Confirmed' ? 'warning' : 'danger'}">${o.status}</span></td>
                                                            <td class="text-end fw-bold">${o.quantity}</td>
                                                        </tr>
                                                    </c:forEach>
                                        </c:when>
                                        <c:otherwise><tr><td colspan="2" class="text-center text-muted">No orders found</td></tr></c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                        <!-- Progress bars for visual -->
                        <div class="mt-3">
                            <c:forEach var="o" items="${orderStatistics}">
                                <div class="mb-2">
                                    <div class="d-flex justify-content-between small">
                                        <span>${o.status}</span>
                                        <span>${o.quantity} orders</span>
                                    </div>
                                    <div class="progress" style="height: 8px;">
                                        <div class="progress-bar bg-${o.status == 'Delivered' ? 'success' : o.status == 'Pending' || o.status == 'Processing' || o.status == 'Confirmed' ? 'warning' : 'danger'}"
                                             role="progressbar" style="width: ${o.quantity / totalOrders * 100}%;"></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==================== PROFIT ==================== -->
            <div id="profit" class="section-card ${currentSection != 'profit' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-chart-line me-2"></i> Profit Details</div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3 text-center mb-3">
                                <div class="p-3 bg-light rounded-3">
                                    <h6 class="text-muted">Revenue</h6>
                                    <div class="h4"><fmt:formatNumber value="${empty revenue ? 0 : revenue}" pattern="#,##0" /> ₫</div>
                                </div>
                            </div>
                            <div class="col-md-3 text-center mb-3">
                                <div class="p-3 bg-light rounded-3">
                                    <h6 class="text-muted">Import Cost</h6>
                                    <div class="h4"><fmt:formatNumber value="${empty totalImportCost ? 0 : totalImportCost}" pattern="#,##0" /> ₫</div>
                                </div>
                            </div>
                            <div class="col-md-3 text-center mb-3">
                                <div class="p-3 bg-light rounded-3">
                                    <h6 class="text-muted">Cost of Goods Sold</h6>
                                    <div class="h4"><fmt:formatNumber value="${empty costOfGoodsSold ? 0 : costOfGoodsSold}" pattern="#,##0" /> ₫</div>
                                </div>
                            </div>
                            <div class="col-md-3 text-center mb-3">
                                <div class="p-3 bg-success bg-opacity-10 rounded-3 border border-success">
                                    <h6 class="text-success">Net Profit</h6>
                                    <div class="h3 text-success"><fmt:formatNumber value="${empty profit ? 0 : profit}" pattern="#,##0" /> ₫</div>
                                </div>
                            </div>
                        </div>
                        <div class="profit-formula">
                            <div class="formula text-center">
                                Profit = Revenue − Cost of Goods Sold
                                &nbsp;&nbsp;→&nbsp;&nbsp;
                                <fmt:formatNumber value="${empty profit ? 0 : profit}" pattern="#,##0" /> ₫
                                &nbsp;=&nbsp;
                                <fmt:formatNumber value="${empty revenue ? 0 : revenue}" pattern="#,##0" /> ₫
                                &nbsp;−&nbsp;
                                <fmt:formatNumber value="${empty costOfGoodsSold ? 0 : costOfGoodsSold}" pattern="#,##0" /> ₫
                            </div>
                        </div>
                        <!-- Import History Summary -->
                        <div class="alert alert-info mt-3">
                            <i class="fas fa-info-circle me-2"></i>
                            <strong>Note:</strong> Import Cost shows total warehouse import value. Cost of Goods Sold is calculated based on average import price of sold items.
                        </div>
                        <!-- Profit by product -->
                        <div class="mt-4">
                            <h6 class="fw-bold"><i class="fas fa-list-ul me-2"></i>Profit Contribution by Product</h6>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped table-sm">
                                    <thead>
                                        <tr><th>Product</th><th class="text-end">Qty Sold</th><th class="text-end">Base Price</th><th class="text-end">Avg Sell Price</th><th class="text-end">Profit / Unit</th><th class="text-end">Total Profit</th></tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty productSales}">
                                                <c:forEach var="p" items="${productSales}">
                                                    <tr>
                                                        <td>${p.productName}</td>
                                                    <td class="text-end">${p.quantitySold}</td>
                                                    <td class="text-end"><fmt:formatNumber value="${p.unitCost}" pattern="#,##0" /> ₫</td>
                                                    <td class="text-end"><fmt:formatNumber value="${p.unitPrice}" pattern="#,##0" /> ₫</td>
                                                    <td class="text-end text-success"><fmt:formatNumber value="${p.quantitySold > 0 ? p.profit / p.quantitySold : 0}" pattern="#,##0" /> ₫</td>
                                                    <td class="text-end fw-bold text-success"><fmt:formatNumber value="${p.profit}" pattern="#,##0" /> ₫</td>
                                                    </tr>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise><tr><td colspan="6" class="text-center text-muted">No product data</td></tr></c:otherwise>
                                        </c:choose>
                                    </tbody>
                                    <tfoot class="table-light fw-bold">
                                        <tr><td colspan="5" class="text-end">TOTAL PROFIT</td>
                                            <td class="text-end text-success"><fmt:formatNumber value="${empty profit ? 0 : profit}" pattern="#,##0" /> ₫</td>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- Toast Notifications -->
<c:if test="${not empty toastMsg}">
<div class="position-fixed top-0 end-0 p-3" style="z-index:9999">
    <div class="toast show align-items-center text-white bg-success border-0" role="alert">
        <div class="d-flex">
            <div class="toast-body"><i class="fas fa-check-circle me-2"></i>${toastMsg}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>
</div>
<script>setTimeout(() => { document.querySelector('.toast')?.remove(); }, 3000);</script>
</c:if>
<c:if test="${not empty toastErr}">
<div class="position-fixed top-0 end-0 p-3" style="z-index:9999">
    <div class="toast show align-items-center text-white bg-danger border-0" role="alert">
        <div class="d-flex">
            <div class="toast-body"><i class="fas fa-exclamation-circle me-2"></i>${toastErr}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>
</div>
<script>setTimeout(() => { document.querySelector('.toast')?.remove(); }, 3000);</script>
</c:if>

<!-- Create Staff Modal -->
<div class="modal fade" id="createAccountModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="fas fa-user-plus me-2"></i>Create Staff Account</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/Admin" method="post" id="createStaffForm">
                <input type="hidden" name="section" value="accounts"/>
                <input type="hidden" name="action" value="createStaff"/>
                <input type="hidden" name="role" value="Staff"/>
                <div class="modal-body">
                    <div class="alert alert-info mb-3">
                        <i class="fas fa-info-circle me-2"></i>
                        <strong>Note:</strong> A random password will be generated and sent to the staff's email address.
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Full Name <span class="text-danger">*</span></label>
                        <input type="text" name="fullName" class="form-control" required placeholder="Nguyen Van A" minlength="2" maxlength="100"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email <span class="text-danger">*</span></label>
                        <input type="email" name="email" class="form-control" required placeholder="staff@example.com"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Phone</label>
                        <input type="tel" name="phone" class="form-control" placeholder="0912 345 678" pattern="[0-9]{9,11}"/>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-success" id="createStaffBtn">
                        <i class="fas fa-paper-plane me-1"></i> Create & Send Email
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>