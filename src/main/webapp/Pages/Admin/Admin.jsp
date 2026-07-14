<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        .sidebar .nav-link.back-home {
            margin-top: 30px; border-top: 1px solid #34495e; padding-top: 20px;
            color: #f1c40f;
        }
        .sidebar .nav-link.back-home:hover { background: #2c3e50; color: #f39c12; }
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
            <div class="brand"><i class="fas fa-chart-pie"></i> Admin</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link ${param.section == null || param.section == 'customerStats' ? 'active' : ''}" 
                       href="?section=customerStats">
                        <i class="fas fa-users"></i> Customer Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'revenueStats' ? 'active' : ''}" 
                       href="?section=revenueStats">
                        <i class="fas fa-dollar-sign"></i> Revenue Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'profitStats' ? 'active' : ''}" 
                       href="?section=profitStats">
                        <i class="fas fa-chart-line"></i> Profit Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'orderStats' ? 'active' : ''}" 
                       href="?section=orderStats">
                        <i class="fas fa-shopping-cart"></i> Order Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link back-home" href="${pageContext.request.contextPath}/home">
                        <i class="fas fa-home"></i> Back to Home
                    </a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">
            <!-- Top summary cards -->
            <div class="row mb-4">
                <div class="col-md-3 col-6">
                    <div class="stat-card">
                        <div class="stat-label"><i class="fas fa-user"></i> Total Customers</div>
                        <div class="stat-number">${totalCustomers}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #3498db;">
                        <div class="stat-label"><i class="fas fa-receipt"></i> Total Orders</div>
                        <div class="stat-number">${totalOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #e67e22;">
                        <div class="stat-label"><i class="fas fa-money-bill-wave"></i> Revenue</div>
                        <div class="stat-number">$${revenue}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #27ae60;">
                        <div class="stat-label"><i class="fas fa-coins"></i> Profit</div>
                        <div class="stat-number">$${profit}</div>
                    </div>
                </div>
            </div>

            <!-- Determine current section -->
            <c:set var="currentSection" value="${param.section}" />
            <c:if test="${empty currentSection}">
                <c:set var="currentSection" value="customerStats" />
            </c:if>

            <!-- Customer Statistics -->
            <div id="customerStats" class="section-card ${currentSection != 'customerStats' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-users me-2"></i> Customer Statistics</span>
                        <form action="${pageContext.request.contextPath}/Admin" method="post" class="search-form">
                            <label for="quantity" class="form-label mb-0">Min orders:</label>
                            <input type="number" name="quantity" id="quantity" class="form-control form-control-sm" value="5" min="1">
                            <input type="hidden" name="section" value="customerStats">
                            <button type="submit" class="btn btn-primary btn-sm"><i class="fas fa-search"></i> Search</button>
                        </form>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead>
                                    <tr><th>#</th><th>Full Name</th><th>Total Orders</th></tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty customerStatistics}">
                                            <c:forEach var="c" items="${customerStatistics}" varStatus="loop">
                                                <tr>
                                                    <td>${loop.index + 1}</td>
                                                    <td>${c.fullName}</td>
                                                    <td>${c.totalOrders}</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="3" class="text-center text-muted">No customers found</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Revenue Statistics -->
            <div id="revenueStats" class="section-card ${currentSection != 'revenueStats' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-dollar-sign me-2"></i> Revenue Statistics</div>
                    <div class="card-body">
                        <h5 class="mb-3">Total Revenue: <span class="text-success">$${revenue}</span></h5>
                        <p class="text-muted">Total income from all paid orders.</p>
                    </div>
                </div>
            </div>

            <!-- Profit Statistics -->
            <div id="profitStats" class="section-card ${currentSection != 'profitStats' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-chart-line me-2"></i> Profit Statistics</div>
                    <div class="card-body">
                        <h5 class="mb-3">Total Profit: <span class="text-success">$${profit}</span></h5>
                        <p class="text-muted">Estimated profit (30% of revenue).</p>
                    </div>
                </div>
            </div>

            <!-- Order Statistics -->
            <div id="orderStats" class="section-card ${currentSection != 'orderStats' ? 'hidden-section' : ''}">
                <div class="card">
                    <div class="card-header"><i class="fas fa-shopping-cart me-2"></i> Order Statistics</div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead>
                                    <tr><th>Status</th><th>Quantity</th></tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty orderStatistics}">
                                            <c:forEach var="o" items="${orderStatistics}">
                                                <tr>
                                                    <td><span class="badge bg-${o.status == 'Completed' ? 'success' : o.status == 'Pending' ? 'warning' : 'danger'}">${o.status}</span></td>
                                                    <td>${o.quantity}</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="2" class="text-center text-muted">No orders found</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>