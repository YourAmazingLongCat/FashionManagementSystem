<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import Bill Detail - Warehouse - Fashion X Store</title>
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

        /* Header Card */
        .bill-header-card {
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: 18px;
            padding: 24px 28px;
            box-shadow: 0 4px 16px rgba(15, 23, 42, 0.03);
            margin-bottom: 24px;
        }
        .bill-title-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 14px;
            margin-bottom: 18px;
        }
        .bill-title {
            font-family: 'Space Grotesk', 'Inter', sans-serif;
            font-size: 1.6rem;
            font-weight: 800;
            margin: 0;
            color: var(--text-dark);
        }
        .bill-meta-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
        }
        .bill-meta-item {
            background: #f8fafc;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 14px 18px;
        }
        .bill-meta-item .lbl {
            font-size: 0.76rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: var(--text-muted);
            margin-bottom: 4px;
        }
        .bill-meta-item .val {
            font-size: 1.1rem;
            font-weight: 800;
            color: var(--text-dark);
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
            background: #ffffff;
        }
        .surface-header-title {
            font-size: 1.15rem;
            font-weight: 800;
            margin: 0;
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

        .sku-tag {
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

        /* Summary Banner */
        .summary-banner {
            margin-top: 24px;
            padding: 22px 28px;
            background: #0f172a;
            border-radius: 18px;
            color: #ffffff;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 18px;
            box-shadow: 0 8px 24px rgba(15, 23, 42, 0.15);
        }
        .summary-banner .lbl {
            font-size: 0.82rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: #94a3b8;
            margin-bottom: 4px;
        }
        .summary-banner .val {
            font-size: 1.65rem;
            font-weight: 800;
            color: #ffffff;
        }

        .btn-back-modern {
            padding: 9px 18px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            background: #ffffff;
            color: var(--text-dark);
            font-weight: 700;
            font-size: 0.88rem;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s ease;
        }
        .btn-back-modern:hover {
            background: #f1f5f9;
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

            <div class="bill-header-card">
                <div class="bill-title-row">
                    <h1 class="bill-title">Import Bill Details</h1>
                    <a class="btn-back-modern" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">
                        <i class="fa-solid fa-arrow-left"></i> Back to Bills
                    </a>
                </div>
                <div class="bill-meta-grid">
                    <div class="bill-meta-item">
                        <div class="lbl">Imported By Staff</div>
                        <div class="val">${billEmployeeName}</div>
                    </div>
                    <div class="bill-meta-item">
                        <div class="lbl">Timestamp</div>
                        <div class="val"><fmt:formatDate value="${billImportedAt}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                    </div>
                    <div class="bill-meta-item">
                        <div class="lbl">Distinct Variants</div>
                        <div class="val">${billRows.size()}</div>
                    </div>
                </div>
            </div>

            <div class="surface-card">
                <div class="surface-header">
                    <h2 class="surface-header-title">Imported Items Breakdown</h2>
                </div>
                <div class="table-responsive">
                    <table class="modern-table">
                        <thead>
                            <tr>
                                <th style="width: 50px;">#</th>
                                <th>Product Name</th>
                                <th>SKU</th>
                                <th class="text-end">Imported Quantity</th>
                                <th class="text-end">Unit Cost</th>
                                <th class="text-end">Line Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty billRows}">
                                    <tr>
                                        <td colspan="6" style="padding: 48px 20px; text-align: center; color: var(--text-muted);">
                                            Bill details not found or deleted.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="grandQty" value="0" />
                                    <c:set var="grandTotal" value="0" />
                                    <c:forEach var="row" items="${billRows}" varStatus="loop">
                                        <c:set var="grandQty" value="${grandQty + row[4]}" />
                                        <c:set var="grandTotal" value="${grandTotal + row[6]}" />
                                        <tr>
                                            <td style="color: var(--text-muted); font-weight: 600;">${loop.count}</td>
                                            <td><strong style="color: var(--text-dark);">${row[2]}</strong></td>
                                            <td><span class="sku-tag">${row[3]}</span></td>
                                            <td class="text-end"><span class="qty-pill-modern">${row[4]}</span></td>
                                            <td class="text-end font-monospace fw-bold"><fmt:formatNumber value="${row[5]}" pattern="#,##0"/> đ</td>
                                            <td class="text-end font-monospace fw-bold" style="color: #059669; font-size: 0.98rem;">
                                                <fmt:formatNumber value="${row[6]}" pattern="#,##0"/> đ
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <c:if test="${not empty billRows}">
                <div class="summary-banner">
                    <div>
                        <div class="lbl">Total Units Added</div>
                        <div class="val">${grandQty} units</div>
                    </div>
                    <div class="text-end">
                        <div class="lbl">Total Inbound Cost</div>
                        <div class="val"><fmt:formatNumber value="${grandTotal}" pattern="#,##0"/> đ</div>
                    </div>
                </div>
            </c:if>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>