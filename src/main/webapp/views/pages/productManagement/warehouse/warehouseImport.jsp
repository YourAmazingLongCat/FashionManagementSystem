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
    <title>Update Quantity - Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { margin: 0; font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; font-size: 0.9rem; color: #2c3e50; background: #f8f9fa; }
        .container-fluid { padding: 0; }
        .row { margin: 0; }
        .row > * { padding: 0; }
        .sidebar {
            background: linear-gradient(180deg, #2c3e50, #1a252f);
            position: sticky; top: 0; height: 100vh; overflow-y: auto;
            align-self: flex-start; padding: 0; color: #ecf0f1;
        }
        .sidebar .brand {
            padding: 20px 15px; font-size: 1.5rem; font-weight: 600;
            border-bottom: 1px solid #34495e; text-align: center; color: #fff;
        }
        .sidebar .nav-link {
            color: #b0c4de; padding: 12px 20px; border-left: 3px solid transparent;
            transition: 0.3s; font-weight: 500; display: block;
            text-decoration: none; font-size: 1rem; line-height: 1.5;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: #34495e; color: #fff; border-left-color: #1abc9c;
        }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); padding: 0; margin: 0; list-style: none; }
        .sidebar .nav-item { list-style: none; }
        .sidebar .nav-item.mt-auto { margin-top: auto; }
        .main-content { padding: 20px 30px; }

        /* Subtabs */
        .warehouse-subtabs {
            display: inline-flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap;
        }
        .warehouse-subtabs a {
            padding: 8px 18px; border-radius: 8px; text-decoration: none;
            color: #334155; font-weight: 600; font-size: 0.88rem;
            display: inline-flex; align-items: center; gap: 8px;
            background: #fff; border: 1px solid #dbe3f0; transition: all 0.2s ease;
        }
        .warehouse-subtabs a:hover {
            background: #f8fafc; color: #1abc9c; border-color: #1abc9c;
        }
        .warehouse-subtabs a.active {
            background: #1abc9c; color: #fff; border-color: #1abc9c;
            box-shadow: 0 4px 12px rgba(26, 188, 156, 0.25);
        }

        /* Page Title */
        .page-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
        .page-title-row h1 { margin: 0; font-size: 1.4rem; font-weight: 700; color: #2c3e50; }
        .page-title-row p { margin: 4px 0 0; color: #64748b; font-size: 0.85rem; }

        /* Surface Card */
        .surface-card {
            background: #ffffff; border: 1px solid #e2e8f0;
            border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            overflow: hidden; margin-bottom: 20px;
        }
        .surface-header {
            padding: 16px 20px; border-bottom: 1px solid #e2e8f0;
            display: flex; justify-content: space-between; align-items: center;
            flex-wrap: wrap; gap: 12px; background: #f8f9fa;
        }
        .surface-header-title { font-size: 1.15rem; font-weight: 700; margin: 0; color: #2c3e50; }

        /* Filter Toolbar */
        .filter-bar-modern {
            padding: 16px 20px; background: #fff; border-bottom: 1px solid #e2e8f0;
            display: flex; gap: 12px; flex-wrap: wrap; align-items: center;
        }
        .input-group-search { position: relative; min-width: 220px; }
        .input-group-search i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; pointer-events: none; }
        .search-input-modern {
            width: 100%; min-height: 38px; padding: 8px 12px 8px 36px;
            border-radius: 8px; border: 1px solid #cbd5e1; background: #fff;
            font: inherit; font-size: 0.9rem; color: #2c3e50; outline: none; transition: 0.2s;
        }
        .search-input-modern:focus { border-color: #1abc9c; box-shadow: 0 0 0 3px rgba(26, 188, 156, 0.15); }
        .select-filter-modern {
            min-height: 38px; padding: 8px 12px; border-radius: 8px; border: 1px solid #cbd5e1;
            background: #fff; font: inherit; font-size: 0.9rem; color: #2c3e50; outline: none; cursor: pointer;
        }
        .select-filter-modern:focus { border-color: #1abc9c; box-shadow: 0 0 0 3px rgba(26, 188, 156, 0.15); }

        /* Buttons */
        .btn-action-primary {
            padding: 8px 18px; border-radius: 8px; border: none;
            background: #1abc9c; color: #fff; font-weight: 600; font-size: 0.88rem;
            display: inline-flex; align-items: center; gap: 8px; cursor: pointer; transition: 0.2s;
        }
        .btn-action-primary:hover { background: #16a085; transform: translateY(-1px); color: #fff; }
        .btn-action-secondary {
            padding: 8px 14px; border-radius: 8px; border: 1px solid #cbd5e1;
            background: #fff; color: #64748b; font-weight: 600; font-size: 0.88rem;
            text-decoration: none; display: inline-flex; align-items: center; gap: 6px; cursor: pointer; transition: 0.2s;
        }
        .btn-action-secondary:hover { background: #f8fafc; color: #2c3e50; }

        /* Table */
        .modern-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
        .modern-table th {
            background: #f1f3f5; color: #2c3e50; font-size: 0.8rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.05em; padding: 12px 14px;
            border: 1px solid #d9dee5; text-align: center; vertical-align: middle;
        }
        .modern-table td {
            padding: 12px 14px; border: 1px solid #e9ecef;
            vertical-align: middle; text-align: center; color: #334155;
        }
        .modern-table tr:hover td { background: #f8fafc; }
        
        .sku-tag {
            background: #f1f5f9; color: #475569; font-weight: 700;
            font-size: 0.78rem; padding: 4px 8px; border-radius: 6px;
            font-family: monospace; display: inline-block; border: 1px solid #e2e8f0;
        }

        .badge-stock {
            display: inline-flex; align-items: center; gap: 5px;
            padding: 4px 10px; border-radius: 20px; font-weight: 700; font-size: 0.76rem;
            text-transform: uppercase; letter-spacing: 0.04em;
        }
        .badge-in-stock { background: #dcfce7; color: #166534; }
        .badge-low { background: #fef3c7; color: #92400e; }
        .badge-out-of-stock { background: #fee2e2; color: #991b1b; }

        .import-inputs { display: flex; gap: 8px; justify-content: center; align-items: center; }
        .qty-input, .price-input {
            height: 36px; padding: 6px 10px; border-radius: 6px; border: 1px solid #cbd5e1;
            font-size: 0.88rem; outline: none; transition: 0.2s; width: 100px;
        }
        .price-input { width: 140px; }
        .qty-input:focus, .price-input:focus { border-color: #1abc9c; box-shadow: 0 0 0 2px rgba(26,188,156,0.15); }
        .qty-input:disabled, .price-input:disabled { background: #f1f5f9; color: #94a3b8; cursor: not-allowed; }

        /* Pagination */
        .pagination-modern {
            padding: 16px 20px; display: flex; justify-content: center; gap: 6px;
            background: #fff; border-top: 1px solid #e2e8f0;
        }
        .page-btn-modern {
            min-width: 36px; height: 36px; padding: 0 10px; border-radius: 6px;
            border: 1px solid #cbd5e1; background: #fff; color: #334155;
            display: inline-flex; align-items: center; justify-content: center;
            text-decoration: none; font-weight: 700; font-size: 0.86rem; transition: 0.2s;
        }
        .page-btn-modern:hover { background: #f8fafc; color: #1abc9c; border-color: #1abc9c; }
        .page-btn-modern.active { background: #1abc9c; color: #fff; border-color: #1abc9c; }

        @media (max-width: 768px) {
            .sidebar { min-height: auto; height: auto; position: static; }
            .main-content { padding: 15px; }
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <jsp:include page="/views/common/staffSidebar.jsp">
            <jsp:param name="activeMenu" value="warehouse" />
        </jsp:include>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">

            <!-- Subtabs -->
            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                    <i class="fas fa-boxes"></i> Inventory
                </a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                    <i class="fas fa-edit"></i> Update Quantity
                </a>
            </div>

            <!-- Page Title -->
            <div class="page-title-row">
                <div>
                    <h1>Update Quantity</h1>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-danger' : 'alert-success'} mb-4" style="border-radius: 8px; font-weight: 600;">
                    ${message}
                </div>
            </c:if>

            <div class="surface-card">
                <!-- Filter Bar -->
                <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import" class="filter-bar-modern" id="inventoryFilterForm">
                    <div class="input-group-search">
                        <i class="fas fa-search"></i>
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
                        <i class="fas fa-filter"></i> Filter
                    </button>
                    <a href="${pageContext.request.contextPath}/staff/warehouse/import" class="btn-action-secondary">Clear</a>
                </form>

                <form method="post" action="${pageContext.request.contextPath}/staff/warehouse/import" id="batchImportForm" onsubmit="return validateBatchForm()">
                    <input type="hidden" name="action" value="import">
                    
                    <div class="surface-header">
                        <h2 class="surface-header-title">Select Variants to Update Quantity</h2>
                        <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
                            <span id="selectedCount" style="font-size: 0.88rem; color: #64748b; font-weight: 700;">0 selected</span>
                            <button type="button" class="btn-action-secondary" id="selectAllBtn">Select all</button>
                            <button type="button" class="btn-action-secondary" id="clearAllBtn">Clear</button>
                            <button type="submit" class="btn-action-primary" id="batchSubmitBtn" style="background: #16a34a;">
                                <i class="fas fa-check"></i> Update Selected
                            </button>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="modern-table">
                            <thead>
                                <tr>
                                    <th style="width: 48px; text-align: center;"><input type="checkbox" id="selectAllCheckbox" style="transform: scale(1.2); cursor: pointer;"></th>
                                    <th>SKU</th>
                                    <th style="text-align: left;">Product</th>
                                    <th>Size / Color</th>
                                    <th class="text-end">Physical</th>
                                    <th class="text-end">Reserved</th>
                                    <th class="text-end">Available</th>
                                    <th>Update Details</th>
                                </tr>
                            </thead>
                            <tbody id="inventoryTableBody">
                                <c:choose>
                                    <c:when test="${empty inventory}">
                                        <tr>
                                            <td colspan="8" style="padding: 48px 20px; text-align: center; color: #64748b;">
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
                                                <td style="text-align: left;"><strong style="color: #2c3e50;">${item[2]}</strong></td>
                                                <td><span style="color: #64748b; font-weight: 500;">Size ${item[4]} &bull; ${item[6]}</span></td>
                                                <td class="text-end font-monospace fw-bold">${physical}</td>
                                                <td class="text-end font-monospace fw-bold" style="color: #2c3e50;">${reserved}</td>
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
                                    <i class="fas fa-chevron-left"></i>
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
                                    <i class="fas fa-chevron-right"></i>
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
