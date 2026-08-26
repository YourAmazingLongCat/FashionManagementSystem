<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Stock In - Warehouse - Fashion X Store</title>
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
        .select-filter-modern {
            padding: 9px 34px 9px 12px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            background: #ffffff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E") no-repeat right 10px center;
            background-size: 14px;
            font-family: inherit;
            font-size: 0.88rem;
            font-weight: 500;
            color: var(--text-dark);
            outline: none;
            appearance: none;
            cursor: pointer;
            transition: all 0.2s ease;
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
            padding: 14px 20px;
            border-bottom: 1px solid var(--border-color);
            white-space: nowrap;
        }
        .modern-table td {
            padding: 14px 20px;
            border-bottom: 1px solid #f1f5f9;
            vertical-align: middle;
        }
        .modern-table tbody tr:hover {
            background: #f8fafc;
        }
        .modern-table tbody tr.selected {
            background: #eff6ff;
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
            padding: 5px 10px;
            border-radius: 999px;
            font-weight: 700;
            font-size: 0.78rem;
        }
        .badge-in-stock { background: #dcfce7; color: #15803d; }
        .badge-low { background: #fee2e2; color: #b91c1c; }

        .import-inputs {
            display: flex;
            gap: 8px;
            align-items: center;
        }
        .import-inputs input {
            padding: 7px 10px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            font-size: 0.88rem;
            font-weight: 600;
            outline: none;
        }
        .import-inputs input:focus {
            border-color: var(--primary);
        }
        .import-inputs .qty-input { width: 85px; }
        .import-inputs .price-input { width: 120px; }

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
                    <h1 class="page-title">Stock In (Batch Import)</h1>
                    <p class="page-subtitle">Select variants, specify incoming quantities and unit cost to generate an import batch.</p>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-danger' : 'alert-success'} mb-4" style="border-radius: 12px; font-weight: 600;">
                    ${message}
                </div>
            </c:if>

            <div class="surface-card">
                <!-- Filter Bar -->
                <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import" class="filter-bar-modern" id="inventoryFilterForm">
                    <div class="input-group-search">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input type="text" class="search-input-modern" name="keyword" placeholder="Search SKU / product..." value="${fn:escapeXml(currentKeyword)}"/>
                    </div>
                    <select class="select-filter-modern" name="productFilter">
                        <option value="">All Products</option>
                        <c:forEach var="p" items="${products}">
                            <option value="${p.productId}" ${currentProductFilter eq p.productId ? 'selected' : ''}>${p.productName}</option>
                        </c:forEach>
                    </select>
                    <select class="select-filter-modern" name="colorFilter">
                        <option value="">All Colors</option>
                        <c:forEach var="c" items="${allColors}">
                            <option value="${c[0]}" ${currentColorFilter eq c[0] ? 'selected' : ''}>${c[1]}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn-action-primary">
                        <i class="fa-solid fa-filter"></i> Filter
                    </button>
                    <a href="${pageContext.request.contextPath}/staff/warehouse/import" class="btn-action-secondary">Clear</a>
                </form>

                <form method="post" action="${pageContext.request.contextPath}/staff/warehouse/import" id="batchImportForm" onsubmit="return validateBatchForm()">
                    <input type="hidden" name="action" value="import">
                    
                    <div class="surface-header">
                        <h2 class="surface-header-title">Select Variants to Stock In</h2>
                        <div style="display: flex; gap: 10px; align-items: center;">
                            <span id="selectedCount" style="font-size: 0.88rem; color: var(--text-muted); font-weight: 700;">0 selected</span>
                            <button type="button" class="btn-action-secondary" id="selectAllBtn">Select all</button>
                            <button type="button" class="btn-action-secondary" id="clearAllBtn">Clear</button>
                            <button type="submit" class="btn-action-primary" id="batchSubmitBtn" style="background: #16a34a;">
                                <i class="fa-solid fa-plus"></i> Import Selected
                            </button>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="modern-table">
                            <thead>
                                <tr>
                                    <th style="width: 48px; text-align: center;"><input type="checkbox" id="selectAllCheckbox" style="transform: scale(1.2); cursor: pointer;"></th>
                                    <th>SKU</th>
                                    <th>Product</th>
                                    <th>Size / Color</th>
                                    <th class="text-end">Physical</th>
                                    <th class="text-end">Reserved</th>
                                    <th class="text-end">Available</th>
                                    <th>Import Details</th>
                                </tr>
                            </thead>
                            <tbody id="inventoryTableBody">
                                <c:choose>
                                    <c:when test="${empty inventory}">
                                        <tr>
                                            <td colspan="8" style="padding: 48px 20px; text-align: center; color: var(--text-muted);">
                                                No product variants found.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="item" items="${inventory}">
                                            <c:set var="physical" value="${item[8]}" />
                                            <c:set var="reserved" value="${item[9]}" />
                                            <c:set var="available" value="${physical - reserved}" />
                                            <tr class="import-row" data-variant-id="${item[0]}">
                                                <td style="text-align: center;">
                                                    <input type="checkbox" class="row-check" name="selectedVariants" value="${item[0]}" style="transform: scale(1.2); cursor: pointer;">
                                                </td>
                                                <td><span class="sku-tag">${item[7]}</span></td>
                                                <td><strong style="color: var(--text-dark);">${item[2]}</strong></td>
                                                <td><span style="color: var(--text-muted); font-weight: 500;">Size ${item[4]} &bull; ${item[6]}</span></td>
                                                <td class="text-end font-monospace fw-bold">${physical}</td>
                                                <td class="text-end font-monospace" style="color: var(--text-muted);">${reserved}</td>
                                                <td class="text-end">
                                                    <span class="badge-stock ${available <= 10 ? 'badge-low' : 'badge-in-stock'}">${available}</span>
                                                </td>
                                                <td>
                                                    <div class="import-inputs">
                                                        <input type="hidden" name="variantId" value="${item[0]}" disabled>
                                                        <input type="number" name="quantity" min="1" placeholder="Qty" class="qty-input" disabled title="Quantity">
                                                        <input type="number" name="importPrice" min="0" step="1000" placeholder="Unit Price (đ)" class="price-input" disabled title="Import Price">
                                                    </div>
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
                                <a href="?tab=import&invPage=${invPage - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}" class="page-btn-modern">
                                    <i class="fa-solid fa-chevron-left"></i>
                                </a>
                            </c:if>
                            <c:forEach begin="1" end="${invTotalPages > 5 ? 5 : invTotalPages}" var="i">
                                <c:set var="invStart" value="${invTotalPages > 5 ? (invPage > 3 ? invPage - 2 : 1) : 1}"/>
                                <a href="?tab=import&invPage=${invStart + i - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}" class="page-btn-modern ${(invStart + i - 1) == invPage ? 'active' : ''}">
                                    ${invStart + i - 1}
                                </a>
                            </c:forEach>
                            <c:if test="${invPage < invTotalPages}">
                                <a href="?tab=import&invPage=${invPage + 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}" class="page-btn-modern">
                                    <i class="fa-solid fa-chevron-right"></i>
                                </a>
                            </c:if>
                        </div>
                    </c:if>
                </form>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function() {
    const form = document.getElementById('batchImportForm');
    const tableBody = document.getElementById('inventoryTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const selectAllBtn = document.getElementById('selectAllBtn');
    const clearAllBtn = document.getElementById('clearAllBtn');
    const selectedCountEl = document.getElementById('selectedCount');
    const submitBtn = document.getElementById('batchSubmitBtn');

    function updateRowEnabled(row, enabled) {
        row.querySelectorAll('input[name="variantId"], input[name="quantity"], input[name="importPrice"]').forEach(function(inp) {
            inp.disabled = !enabled;
        });
        if (enabled) {
            row.classList.add('selected');
        } else {
            row.classList.remove('selected');
        }
    }

    function refreshCount() {
        const checks = tableBody.querySelectorAll('.row-check');
        let count = 0;
        checks.forEach(function(c) { if (c.checked) count++; });
        selectedCountEl.textContent = count + ' selected';
        submitBtn.disabled = count === 0;
        submitBtn.style.opacity = count === 0 ? '0.5' : '1';
    }

    tableBody.addEventListener('change', function(e) {
        if (e.target.classList.contains('row-check')) {
            const row = e.target.closest('tr');
            updateRowEnabled(row, e.target.checked);
            if (e.target.checked) {
                const qty = row.querySelector('input[name="quantity"]');
                if (qty && !qty.value) qty.focus();
            }
            refreshCount();
        }
    });

    selectAllCheckbox.addEventListener('change', function() {
        const checks = tableBody.querySelectorAll('.row-check');
        checks.forEach(function(c) {
            c.checked = selectAllCheckbox.checked;
            updateRowEnabled(c.closest('tr'), selectAllCheckbox.checked);
        });
        refreshCount();
    });

    selectAllBtn.addEventListener('click', function() {
        selectAllCheckbox.checked = true;
        selectAllCheckbox.dispatchEvent(new Event('change'));
    });

    clearAllBtn.addEventListener('click', function() {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.dispatchEvent(new Event('change'));
    });

    window.validateBatchForm = function() {
        const checks = tableBody.querySelectorAll('.row-check:checked');
        if (checks.length === 0) {
            alert('Please select at least one variant.');
            return false;
        }
        let invalid = 0;
        checks.forEach(function(c) {
            const row = c.closest('tr');
            const qty = row.querySelector('input[name="quantity"]');
            if (!qty.value || parseInt(qty.value) <= 0) {
                qty.style.borderColor = '#dc2626';
                invalid++;
            } else {
                qty.style.borderColor = '';
            }
        });
        if (invalid > 0) {
            alert('Please enter a valid quantity (>0) for each selected variant.');
            return false;
        }
        submitBtn.textContent = 'Importing...';
        submitBtn.disabled = true;
        return true;
    };

    refreshCount();
})();
</script>
</body>
</html>
