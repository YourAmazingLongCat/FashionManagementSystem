<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Warehouse Inventory - Fashion X Store</title>
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

        /* Sidebar Styling */
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

        /* Page Title */
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

        /* Stat Cards */
        .stat-card-modern {
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 22px 24px;
            box-shadow: 0 2px 10px rgba(15, 23, 42, 0.03);
            display: flex;
            align-items: center;
            justify-content: space-between;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .stat-card-modern:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
        }
        .stat-label {
            font-size: 0.82rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: var(--text-muted);
            margin-bottom: 6px;
        }
        .stat-value {
            font-family: 'Space Grotesk', 'Inter', sans-serif;
            font-size: 1.85rem;
            font-weight: 800;
            color: var(--text-dark);
            line-height: 1;
        }
        .stat-icon-wrap {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.3rem;
        }
        .stat-icon-blue { background: #eff6ff; color: #2563eb; }
        .stat-icon-purple { background: #f5f3ff; color: #7c3aed; }
        .stat-icon-emerald { background: #ecfdf5; color: #059669; }
        .stat-icon-amber { background: #fffbeb; color: #d97706; }

        /* Surface Card & Toolbar */
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

        .search-toolbar-modern {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
        }
        .input-group-search {
            position: relative;
            min-width: 260px;
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
        .select-filter-modern {
            padding: 9px 34px 9px 14px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            background: #ffffff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E") no-repeat right 10px center;
            background-size: 14px;
            font-family: inherit;
            font-size: 0.88rem;
            font-weight: 500;
            color: var(--text-dark);
            outline: none;
            cursor: pointer;
            appearance: none;
            transition: all 0.2s ease;
        }
        .select-filter-modern:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.1);
        }
        .btn-search-modern {
            padding: 9px 18px;
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
        .btn-search-modern:hover {
            background: #1e293b;
            transform: translateY(-1px);
        }
        .btn-reset-modern {
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
        .btn-reset-modern:hover {
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

        .product-thumb-img {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            object-fit: cover;
            border: 1px solid var(--border-color);
            background: #ffffff;
        }
        .no-thumb {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            background: #f1f5f9;
            border: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #94a3b8;
            font-size: 0.75rem;
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

        .badge-stock {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 0.78rem;
            font-weight: 700;
            letter-spacing: 0.02em;
        }
        .badge-in-stock { background: #ecfdf5; color: #059669; }
        .badge-medium { background: #fefce8; color: #ca8a04; }
        .badge-low { background: #fff7ed; color: #ea580c; }
        .badge-out-of-stock { background: #fef2f2; color: #dc2626; }

        .reserved-pill {
            background: #f1f5f9;
            color: #64748b;
            padding: 2px 8px;
            border-radius: 6px;
            font-weight: 700;
            font-size: 0.82rem;
        }

        /* Empty State */
        .empty-state-modern {
            padding: 56px 24px;
            text-align: center;
            color: var(--text-muted);
        }
        .empty-state-icon {
            font-size: 2.5rem;
            color: #cbd5e1;
            margin-bottom: 12px;
        }
        .empty-state-title {
            font-size: 1.1rem;
            font-weight: 700;
            color: var(--text-dark);
            margin: 0 0 6px;
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
            .surface-header { flex-direction: column; align-items: stretch; }
            .search-toolbar-modern { width: 100%; }
            .input-group-search { min-width: 100%; }
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

            <!-- Subtabs Navigation -->
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

            <!-- Page Title Header -->
            <div class="page-header-row">
                <div>
                    <h1 class="page-title">Warehouse Inventory</h1>
                    <p class="page-subtitle">Track physical stock, reserved units, and real-time available stock.</p>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-danger' : 'alert-success'} mb-4" style="border-radius: 12px; font-weight: 600;">
                    ${message}
                </div>
            </c:if>

            <!-- Stat Metric Cards -->
            <div class="row g-3 mb-4">
                <div class="col-xl-3 col-sm-6">
                    <div class="stat-card-modern">
                        <div>
                            <div class="stat-label">Total Variants</div>
                            <div class="stat-value">${totalItems}</div>
                        </div>
                        <div class="stat-icon-wrap stat-icon-blue">
                            <i class="fa-solid fa-shapes"></i>
                        </div>
                    </div>
                </div>
                <div class="col-xl-3 col-sm-6">
                    <div class="stat-card-modern">
                        <div>
                            <div class="stat-label">Total Physical Stock</div>
                            <div class="stat-value">${totalStock}</div>
                        </div>
                        <div class="stat-icon-wrap stat-icon-purple">
                            <i class="fa-solid fa-boxes-packing"></i>
                        </div>
                    </div>
                </div>
                <div class="col-xl-3 col-sm-6">
                    <div class="stat-card-modern">
                        <div>
                            <div class="stat-label">Total Available</div>
                            <div class="stat-value">${totalAvailable}</div>
                        </div>
                        <div class="stat-icon-wrap stat-icon-emerald">
                            <i class="fa-solid fa-circle-check"></i>
                        </div>
                    </div>
                </div>
                <div class="col-xl-3 col-sm-6">
                    <div class="stat-card-modern">
                        <div>
                            <div class="stat-label">Low Stock Items</div>
                            <div class="stat-value" style="${lowStockCount > 0 ? 'color: #d97706;' : ''}">${lowStockCount}</div>
                        </div>
                        <div class="stat-icon-wrap stat-icon-amber">
                            <i class="fa-solid fa-triangle-exclamation"></i>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Table Card -->
            <div class="surface-card">
                <div class="surface-header">
                    <h2 class="surface-header-title">Product Stock List</h2>

                    <!-- Filter & Search Toolbar -->
                    <form class="search-toolbar-modern" method="get" action="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <div class="input-group-search">
                            <i class="fa-solid fa-magnifying-glass"></i>
                            <input class="search-input-modern" type="text" name="keyword" value="${currentKeyword}" placeholder="Search by product name or SKU..." />
                        </div>
                        <select class="select-filter-modern" name="productFilter">
                            <option value="">All Products</option>
                            <c:forEach var="p" items="${products}">
                                <option value="${p.productId}" ${currentProductFilter eq p.productId ? 'selected' : ''}>${p.productName}</option>
                            </c:forEach>
                        </select>
                        <select class="select-filter-modern" name="colorFilter">
                            <option value="">All Colors</option>
                            <c:forEach var="color" items="${allColors}">
                                <option value="${color[0]}" ${currentColorFilter eq color[0] ? 'selected' : ''}>${color[1]}</option>
                            </c:forEach>
                        </select>
                        <button class="btn-search-modern" type="submit">
                            <i class="fa-solid fa-filter"></i> Apply
                        </button>
                        <c:if test="${not empty currentKeyword or not empty currentProductFilter or not empty currentColorFilter}">
                            <a class="btn-reset-modern" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Reset</a>
                        </c:if>
                    </form>
                </div>

                <div class="table-responsive">
                    <table class="modern-table">
                        <thead>
                            <tr>
                                <th style="width: 60px;">Image</th>
                                <th>SKU</th>
                                <th>Product Name</th>
                                <th>Category</th>
                                <th>Price</th>
                                <th>Size & Color</th>
                                <th class="text-end">Physical Stock</th>
                                <th class="text-end">Reserved</th>
                                <th class="text-end">Available</th>
                                <th class="text-end">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty inventory}">
                                    <tr>
                                        <td colspan="10">
                                            <div class="empty-state-modern">
                                                <div class="empty-state-icon">
                                                    <i class="fa-solid fa-box-open"></i>
                                                </div>
                                                <h3 class="empty-state-title">No inventory records found</h3>
                                                <p>Try refining your search terms or filters.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="item" items="${inventory}">
                                        <c:set var="physical" value="${item[8]}" />
                                        <c:set var="reserved" value="${item[9]}" />
                                        <c:set var="available" value="${physical - reserved}" />
                                        <tr>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty item[14]}">
                                                        <img class="product-thumb-img" src="${pageContext.request.contextPath}${item[14]}" alt="${item[2]}">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="no-thumb"><i class="fa-solid fa-image"></i></div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><span class="sku-tag">${item[7]}</span></td>
                                            <td><strong style="color: var(--text-dark);">${item[2]}</strong></td>
                                            <td><span style="color: var(--text-muted); font-size: 0.88rem;">${item[13]}</span></td>
                                            <td class="font-monospace fw-bold" style="color: var(--text-dark); font-size: 0.9rem;">
                                                <fmt:formatNumber value="${empty item[10] ? item[12] : item[10]}" type="number" groupingUsed="true" /> đ
                                            </td>
                                            <td>
                                                <span style="color: var(--text-muted); font-weight: 500;">Size ${item[4]} &bull; ${item[6]}</span>
                                            </td>
                                            <td class="text-end font-monospace fw-bold" style="font-size: 1rem;">${physical}</td>
                                            <td class="text-end">
                                                <c:choose>
                                                    <c:when test="${reserved > 0}">
                                                        <span class="reserved-pill">${reserved}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color: #cbd5e1; font-weight: 600;">0</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end font-monospace fw-bold" style="font-size: 1.05rem; color: ${available == 0 ? '#ef4444' : '#0f172a'};">
                                                ${available}
                                            </td>
                                            <td class="text-end">
                                                <c:choose>
                                                    <c:when test="${available == 0}">
                                                        <span class="badge-stock badge-out-of-stock">Out of Stock</span>
                                                    </c:when>
                                                    <c:when test="${available <= 5}">
                                                        <span class="badge-stock badge-low">Low Stock</span>
                                                    </c:when>
                                                    <c:when test="${available <= 20}">
                                                        <span class="badge-stock badge-medium">Medium</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-stock badge-in-stock">In Stock</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <c:if test="${invTotalPages > 1}">
                    <div class="pagination-modern">
                        <c:if test="${invPage > 1}">
                            <a class="page-btn-modern" href="?activeTab=inventory&invPage=${invPage - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}">
                                <i class="fa-solid fa-chevron-left"></i>
                            </a>
                        </c:if>
                        <c:forEach begin="1" end="${invTotalPages > 5 ? 5 : invTotalPages}" var="i">
                            <c:set var="invStart" value="${invTotalPages > 5 ? (invPage > 3 ? invPage - 2 : 1) : 1}"/>
                            <a class="page-btn-modern ${(invStart + i - 1) == invPage ? 'active' : ''}" href="?activeTab=inventory&invPage=${invStart + i - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}">
                                ${invStart + i - 1}
                            </a>
                        </c:forEach>
                        <c:if test="${invPage < invTotalPages}">
                            <a class="page-btn-modern" href="?activeTab=inventory&invPage=${invPage + 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}">
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
