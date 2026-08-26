<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import Bills - Warehouse - Fashion X Store</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Space+Grotesk:wght@600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --bg-page: #f8fafc;
            --sidebar-bg: #0f172a;
            --sidebar-hover: #1e293b;
            --sidebar-active: #334155;
            --sidebar-text: #94a3b8;
            --sidebar-text-active: #ffffff;
            --primary: #0f172a;
            --accent: #2563eb;
            --border-color: #e2e8f0;
            --card-bg: #ffffff;
            --text-dark: #0f172a;
            --text-muted: #64748b;
        }

        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            color: var(--text-dark);
            background: var(--bg-page);
            -webkit-font-smoothing: antialiased;
        }

        /* Sidebar */
        .sidebar {
            background: var(--sidebar-bg);
            position: sticky;
            top: 0;
            height: 100vh;
            overflow-y: auto;
            align-self: flex-start;
            padding: 0;
            color: #ecf0f1;
            box-shadow: 2px 0 12px rgba(0,0,0,0.08);
            z-index: 100;
        }
        .sidebar .brand {
            padding: 24px 20px;
            font-family: 'Space Grotesk', sans-serif;
            font-size: 1.25rem;
            font-weight: 800;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            border-bottom: 1px solid rgba(255,255,255,0.08);
            display: flex;
            align-items: center;
            gap: 10px;
            color: #ffffff;
        }
        .sidebar .brand .brand-badge {
            background: #ef4444;
            color: #fff;
            padding: 2px 7px;
            border-radius: 4px;
            font-size: 0.78rem;
            font-weight: 900;
        }
        .sidebar .nav {
            display: flex;
            flex-direction: column;
            min-height: calc(100vh - 85px);
            padding: 16px 12px;
            margin: 0;
            list-style: none;
            gap: 4px;
        }
        .sidebar .nav-link {
            color: var(--sidebar-text);
            padding: 12px 16px;
            border-radius: 10px;
            transition: all 0.2s ease;
            font-weight: 600;
            font-size: 0.92rem;
            display: flex;
            align-items: center;
            gap: 12px;
            text-decoration: none;
        }
        .sidebar .nav-link:hover {
            background: var(--sidebar-hover);
            color: #f1f5f9;
        }
        .sidebar .nav-link.active {
            background: var(--sidebar-active);
            color: var(--sidebar-text-active);
            box-shadow: 0 2px 8px rgba(0,0,0,0.18);
        }
        .sidebar .nav-link i {
            width: 20px;
            text-align: center;
            font-size: 1.05rem;
            opacity: 0.85;
        }
        .sidebar .nav-item.mt-auto { margin-top: auto; }
        .sidebar .nav-divider {
            height: 1px;
            background: rgba(255,255,255,0.08);
            margin: 12px 0;
        }

        /* Main Content */
        .main-content {
            padding: 32px 36px;
            max-width: 1600px;
        }

        /* Subtabs */
        .warehouse-subtabs {
            display: inline-flex;
            gap: 6px;
            padding: 6px;
            background: #ffffff;
            border-radius: 14px;
            border: 1px solid var(--border-color);
            box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
            margin-bottom: 28px;
        }
        .warehouse-subtabs a {
            padding: 10px 22px;
            border-radius: 10px;
            text-decoration: none;
            color: var(--text-muted);
            font-weight: 700;
            font-size: 0.92rem;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s ease;
        }
        .warehouse-subtabs a:hover {
            background: #f1f5f9;
            color: var(--text-dark);
        }
        .warehouse-subtabs a.active {
            background: var(--primary);
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(15, 23, 42, 0.15);
        }

        /* Header */
        .page-header-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
        }
        .page-title {
            font-family: 'Space Grotesk', 'Inter', sans-serif;
            font-size: 1.75rem;
            font-weight: 800;
            letter-spacing: -0.02em;
            margin: 0;
            color: var(--text-dark);
        }
        .page-subtitle {
            margin: 4px 0 0;
            color: var(--text-muted);
            font-size: 0.9rem;
        }

        /* Surface Card */
        .surface-card {
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: 18px;
            box-shadow: 0 4px 16px rgba(15, 23, 42, 0.03);
            overflow: hidden;
            margin-bottom: 24px;
        }
        .surface-header {
            padding: 20px 28px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            background: #ffffff;
        }
        .surface-header-title {
            font-size: 1.15rem;
            font-weight: 800;
            margin: 0;
            color: var(--text-dark);
        }

        /* Filter Toolbar */
        .filter-bar-modern {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
            padding: 18px 28px;
            background: #f8fafc;
            border-bottom: 1px solid var(--border-color);
        }
        .input-group-search {
            position: relative;
            min-width: 240px;
        }
        .input-group-search i {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-muted);
            font-size: 0.9rem;
            pointer-events: none;
        }
        .search-input-modern {
            width: 100%;
            padding: 9px 14px 9px 36px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            background: #ffffff;
            font-family: inherit;
            font-size: 0.88rem;
            color: var(--text-dark);
            outline: none;
            transition: all 0.2s ease;
        }
        .search-input-modern:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.1);
        }
        .select-filter-modern, .date-filter-modern {
            padding: 9px 14px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            background: #ffffff;
            font-family: inherit;
            font-size: 0.88rem;
            font-weight: 500;
            color: var(--text-dark);
            outline: none;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .select-filter-modern {
            padding-right: 34px;
            background: #ffffff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E") no-repeat right 10px center;
            background-size: 14px;
            appearance: none;
        }
        .btn-action-primary {
            padding: 9px 20px;
            border-radius: 10px;
            border: none;
            background: var(--primary);
            color: #ffffff;
            font-weight: 700;
            font-size: 0.88rem;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .btn-action-primary:hover {
            background: #1e293b;
            transform: translateY(-1px);
        }
        .btn-action-secondary {
            padding: 9px 16px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            background: #ffffff;
            color: var(--text-muted);
            font-weight: 600;
            font-size: 0.88rem;
            text-decoration: none;
            transition: all 0.2s ease;
        }
        .btn-action-secondary:hover {
            background: #f1f5f9;
            color: var(--text-dark);
        }

        /* Modern Table */
        .modern-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.92rem;
        }
        .modern-table th {
            background: #f8fafc;
            color: #475569;
            font-size: 0.76rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            padding: 16px 20px;
            border-bottom: 1px solid var(--border-color);
            white-space: nowrap;
        }
        .modern-table td {
            padding: 16px 20px;
            border-bottom: 1px solid #f1f5f9;
            vertical-align: middle;
        }
        .modern-table tbody tr:hover {
            background: #f8fafc;
        }

        .bill-id-tag {
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 0.82rem;
            font-weight: 700;
            background: #f1f5f9;
            color: #0f172a;
            padding: 4px 8px;
            border-radius: 6px;
            border: 1px solid #e2e8f0;
            display: inline-block;
        }

        .qty-pill-modern {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 50px;
            padding: 4px 10px;
            border-radius: 999px;
            font-weight: 700;
            font-size: 0.82rem;
            background: #ecfdf5;
            color: #059669;
        }

        .btn-view-modern {
            padding: 7px 14px;
            background: #ffffff;
            color: var(--primary);
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            font-size: 0.82rem;
            font-weight: 700;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: all 0.2s ease;
        }
        .btn-view-modern:hover {
            background: var(--primary);
            color: #ffffff;
            border-color: var(--primary);
        }

        /* Pagination */
        .pagination-modern {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 8px;
            padding: 20px;
        }
        .page-btn-modern {
            min-width: 36px;
            height: 36px;
            padding: 0 12px;
            border-radius: 8px;
            border: 1px solid #e2e8f0;
            background: #ffffff;
            color: var(--text-dark);
            display: inline-flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            font-weight: 700;
            font-size: 0.86rem;
            transition: all 0.2s ease;
        }
        .page-btn-modern:hover {
            background: #f1f5f9;
            color: var(--primary);
        }
        .page-btn-modern.active {
            background: var(--primary);
            color: #ffffff;
            border-color: var(--primary);
        }

        @media (max-width: 992px) {
            .sidebar { height: auto; position: relative; }
            .sidebar .nav { min-height: auto; }
            .main-content { padding: 20px; }
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand">
                <span>FASHION</span>
                <span class="brand-badge">X</span>
                <span>STORE</span>
            </div>
            <ul class="nav">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">
                        <i class="fa-solid fa-cart-shopping"></i> Manage Orders
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">
                        <i class="fa-solid fa-shirt"></i> Manage Products
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">
                        <i class="fa-solid fa-layer-group"></i> Manage Variants
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <i class="fa-solid fa-warehouse"></i> Manage Warehouse
                    </a>
                </li>
                <li class="nav-divider"></li>
                <li class="nav-item mt-auto">
                    <a class="nav-link" href="${pageContext.request.contextPath}/profile">
                        <i class="fa-solid fa-user"></i> Profile
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">
                        <i class="fa-solid fa-arrow-right-from-bracket"></i> Logout
                    </a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">

            <!-- Subtabs -->
            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                    <i class="fa-solid fa-boxes-stacked"></i> Inventory
                </a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                    <i class="fa-solid fa-dolly"></i> Stock In
                </a>
                <a class="${activeTab eq 'import-bills' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">
                    <i class="fa-solid fa-file-invoice-dollar"></i> Import Bills
                </a>
            </div>

            <!-- Page Title -->
            <div class="page-header-row">
                <div>
                    <h1 class="page-title">Import Bills History</h1>
                    <p class="page-subtitle">Track historical inbound warehouse batches, total costs, and receipts.</p>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-danger' : 'alert-success'} mb-4" style="border-radius: 12px; font-weight: 600;">
                    ${message}
                </div>
            </c:if>

            <div class="surface-card">
                <!-- Filter Bar -->
                <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import-bills" class="filter-bar-modern">
                    <div class="input-group-search">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input type="text" class="search-input-modern" name="search" value="${billSearch}" placeholder="Search by importer name..." />
                    </div>
                    <select class="select-filter-modern" name="importerFilter">
                        <option value="">All Importers</option>
                        <c:forEach var="imp" items="${importers}">
                            <option value="${imp[0]}" ${importerFilter eq imp[0] ? 'selected' : ''}>${imp[1]}</option>
                        </c:forEach>
                    </select>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 0.82rem; font-weight: 600; color: var(--text-muted);">From:</span>
                        <input type="date" class="date-filter-modern" name="dateFrom" value="${dateFrom}" />
                    </div>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 0.82rem; font-weight: 600; color: var(--text-muted);">To:</span>
                        <input type="date" class="date-filter-modern" name="dateTo" value="${dateTo}" />
                    </div>
                    <button type="submit" class="btn-action-primary">
                        <i class="fa-solid fa-filter"></i> Filter
                    </button>
                    <a href="${pageContext.request.contextPath}/staff/warehouse/import-bills" class="btn-action-secondary">Clear</a>
                </form>

                <div class="surface-header">
                    <h2 class="surface-header-title">Import History List</h2>
                </div>

                <div class="table-responsive">
                    <table class="modern-table">
                        <thead>
                            <tr>
                                <th>Bill ID</th>
                                <th>Imported By</th>
                                <th>Timestamp</th>
                                <th class="text-end">Distinct Items</th>
                                <th class="text-end">Total Quantity</th>
                                <th class="text-end">Total Amount</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty importBills}">
                                    <tr>
                                        <td colspan="7" style="padding: 48px 20px; text-align: center; color: var(--text-muted);">
                                            No import bills found.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="bill" items="${importBills}">
                                        <tr>
                                            <td><span class="bill-id-tag">BILL-${bill[1].toString().replace(' ', 'T').replace(':', '').replace('.', '').substring(0, 12)}</span></td>
                                            <td><strong style="color: var(--text-dark);">${bill[3]}</strong></td>
                                            <td><fmt:formatDate value="${bill[1]}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                            <td class="text-end font-monospace fw-bold">${bill[4]}</td>
                                            <td class="text-end"><span class="qty-pill-modern">${bill[5]}</span></td>
                                            <td class="text-end font-monospace fw-bold" style="color: var(--text-dark); font-size: 0.96rem;">
                                                <fmt:formatNumber value="${bill[6]}" pattern="#,##0"/> đ
                                            </td>
                                            <td class="text-center">
                                                <c:url value="/staff/warehouse/import-bills/view" var="billDetailUrl">
                                                    <c:param name="billKey" value="${bill[0]}" />
                                                </c:url>
                                                <a class="btn-view-modern" href="${billDetailUrl}">
                                                    <i class="fa-solid fa-eye"></i> View Detail
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <c:if test="${billTotalPages > 1}">
                    <div class="pagination-modern">
                        <c:if test="${billPage > 1}">
                            <a class="page-btn-modern" href="?billPage=${billPage - 1}&search=${fn:escapeXml(billSearch)}&importerFilter=${importerFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">
                                <i class="fa-solid fa-chevron-left"></i>
                            </a>
                        </c:if>
                        <c:forEach begin="1" end="${billTotalPages > 5 ? 5 : billTotalPages}" var="i">
                            <c:set var="billStart" value="${billTotalPages > 5 ? (billPage > 3 ? (billPage + 2 > billTotalPages ? billTotalPages - 4 : billPage - 2) : 1) : 1}"/>
                            <a class="page-btn-modern ${(billStart + i - 1) == billPage ? 'active' : ''}" href="?billPage=${billStart + i - 1}&search=${fn:escapeXml(billSearch)}&importerFilter=${importerFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">
                                ${billStart + i - 1}
                            </a>
                        </c:forEach>
                        <c:if test="${billPage < billTotalPages}">
                            <a class="page-btn-modern" href="?billPage=${billPage + 1}&search=${fn:escapeXml(billSearch)}&importerFilter=${importerFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">
                                <i class="fa-solid fa-chevron-right"></i>
                            </a>
                        </c:if>
                    </div>
                </c:if>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>