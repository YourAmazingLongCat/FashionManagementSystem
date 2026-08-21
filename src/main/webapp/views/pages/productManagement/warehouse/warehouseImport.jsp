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
    <title>Stock In - Warehouse</title>
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
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .card-header { background: #f8f9fa; font-weight: 600; }
        .warehouse-subtabs {
            display: flex; gap: 6px; padding: 6px;
            background: #fff; border-radius: 12px; border: 1px solid #e2e8f0;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04); margin-bottom: 20px; flex-wrap: wrap;
        }
        .warehouse-subtabs a {
            padding: 10px 18px; border-radius: 8px; text-decoration: none;
            color: #475569; font-weight: 600; font-size: 0.9rem;
            display: inline-flex; align-items: center; gap: 8px;
            transition: 0.2s;
        }
        .warehouse-subtabs a:hover { background: #f1f5f9; color: #1e293b; }
        .warehouse-subtabs a.active {
            background: linear-gradient(135deg, #1abc9c, #16a085);
            color: #fff;
        }
        .alert { padding: 14px 16px; border-radius: 12px; font-weight: 600; margin-bottom: 18px; }
        .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
        .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
        .table-panel { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; margin-bottom: 24px; }
        .table-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
        .table-header h3 { margin: 0; font-size: 1.05rem; }
        .table-wrapper { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f1f5f9; }
        th { background: #f8fafc; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #64748b; font-weight: 700; white-space: nowrap; }
        tr:hover { background: #fafbff; }
        .stock-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 60px; padding: 4px 10px; border-radius: 999px; font-weight: 700; font-size: 0.8rem; }
        .stock-high { background: rgba(22, 163, 74, 0.12); color: #16a34a; }
        .stock-low { background: rgba(220, 38, 38, 0.12); color: #dc2626; }
        .btn { padding: 8px 16px; border-radius: 8px; border: none; font-weight: 700; font-size: 0.85rem; cursor: pointer; }
        .btn-primary { background: #1abc9c; color: #ffffff; }
        .btn-primary:hover { background: #16a085; }
        .reset-btn { padding: 9px 16px; background: #fff; color: #64748b; border: 1px solid #dbe3f0; border-radius: 10px; font-size: 0.88rem; font-weight: 600; text-decoration: none; cursor: pointer; display: inline-flex; align-items: center; }
        .reset-btn:hover { background: #f1f5f9; }
        .import-form { display: flex; gap: 8px; align-items: center; }
        .import-form input[type="number"] { width: 80px; padding: 8px; border-radius: 8px; border: 1px solid #dbe3f0; }
        .import-form input[type="number"].price-input { width: 100px; }
        .import-row.selected td { background: rgba(26, 188, 156, 0.04); }
        .history-section { margin-top: 24px; }
        .empty-state { padding: 32px; text-align: center; color: #64748b; }
        .filter-bar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; padding: 16px 20px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
        .filter-bar .filter-group { display: flex; align-items: center; gap: 6px; }
        .filter-bar .filter-group label { font-size: 0.8rem; font-weight: 600; color: #64748b; white-space: nowrap; }
        .filter-bar select, .filter-bar input[type="text"] {
            padding: 6px 10px; border: 1px solid #dbe3f0; border-radius: 8px;
            font-size: 0.85rem; outline: none; background: #ffffff; min-width: 130px;
        }
        .filter-bar select:focus, .filter-bar input[type="text"]:focus { border-color: #1abc9c; box-shadow: 0 0 0 2px rgba(26,188,156,0.15); }
        .filter-bar .btn-filter { padding: 6px 14px; background: #1abc9c; color: #fff; border: none; border-radius: 8px; font-size: 0.82rem; font-weight: 600; cursor: pointer; }
        .filter-bar .btn-filter:hover { background: #16a085; }
        .filter-bar .btn-clear { padding: 6px 12px; background: #fff; color: #64748b; border: 1px solid #dbe3f0; border-radius: 8px; font-size: 0.82rem; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; }
        .filter-bar .btn-clear:hover { background: #f1f5f9; }
        .page-btn { display: inline-flex; align-items: center; justify-content: center; min-width: 36px; height: 36px; padding: 0 10px; border-radius: 10px; border: 1px solid #dbe3f0; background: #fff; color: #334155; font-size: 0.85rem; font-weight: 600; text-decoration: none; transition: all 0.2s ease; }
        .page-btn:hover { background: #f1f5f9; border-color: #1abc9c; color: #1abc9c; }
        .page-btn.active { background: #1abc9c; border-color: #1abc9c; color: #fff; }
        @media (max-width: 768px) { .sidebar { min-height: auto; height: auto; } .main-content { padding: 15px; } }
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
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Manage Orders</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/payments">Manage Payments</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">Manage Products</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Manage Variants</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Manage Warehouse</a>
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

            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Inventory</a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">Stock In</a>
                <a class="${activeTab eq 'import-bills' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">Import Bills</a>
            </div>

            <div class="mb-3">
                <h4 class="mb-0">Stock In</h4>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-error' : 'alert-success'}">${message}</div>
            </c:if>

                <!-- Filter & Search Bar -->
                <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import" class="filter-bar" id="inventoryFilterForm">
                    <div class="filter-group">
                        <label>Search:</label>
                        <input type="text" name="keyword" placeholder="Search SKU / product..." value="${fn:escapeXml(currentKeyword)}"/>
                    </div>
                    <div class="filter-group">
                        <label>Product:</label>
                        <select name="productFilter">
                            <option value="">All Products</option>
                            <c:forEach var="p" items="${products}">
                                <option value="${p.productId}" ${currentProductFilter eq p.productId ? 'selected' : ''}>${p.productName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label>Color:</label>
                        <select name="colorFilter">
                            <option value="">All Colors</option>
                            <c:forEach var="c" items="${allColors}">
                                <option value="${c[0]}" ${currentColorFilter eq c[0] ? 'selected' : ''}>${c[1]}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn-filter">Filter</button>
                    <a href="${pageContext.request.contextPath}/staff/warehouse/import" class="btn-clear">Clear</a>
                </form>

                <form method="post" action="${pageContext.request.contextPath}/staff/warehouse/import" id="batchImportForm" onsubmit="return validateBatchForm()">
                    <input type="hidden" name="action" value="import">
                    <div class="table-panel">
                        <div class="table-header">
                            <h3>Variant List</h3>
                            <div style="display: flex; gap: 8px; align-items: center;">
                                <span id="selectedCount" style="font-size: 0.85rem; color: #64748b; font-weight: 600;">0 selected</span>
                                <button type="button" class="btn" style="background:#fff; color:#64748b; border:1px solid #dbe3f0;" id="selectAllBtn">Select all</button>
                                <button type="button" class="btn" style="background:#fff; color:#64748b; border:1px solid #dbe3f0;" id="clearAllBtn">Clear</button>
                                <button type="submit" class="btn btn-primary" id="batchSubmitBtn">+ Add selected</button>
                            </div>
                        </div>
                        <div class="table-wrapper">
                            <table>
                                <thead>
                                    <tr>
                                        <th style="width: 44px;"><input type="checkbox" id="selectAllCheckbox"></th>
                                        <th>SKU</th>
                                        <th>Product</th>
                                        <th>Size / Color</th>
                                        <th class="text-end">Physical</th>
                                        <th class="text-end">Reserved</th>
                                        <th class="text-end">Available</th>
                                        <th>Add Stock</th>
                                    </tr>
                                </thead>
                                <tbody id="inventoryTableBody">
                                    <c:choose>
                                        <c:when test="${empty inventory}">
                                            <tr>
                                                <td colspan="8">
                                                    <div class="empty-state">No product variants found. Check that Products and ProductVariants tables have data.</div>
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="item" items="${inventory}">
                                                <c:set var="physical" value="${item[8]}" />
                                                <c:set var="reserved" value="${item[9]}" />
                                                <c:set var="available" value="${physical - reserved}" />
                                                <tr class="import-row" data-variant-id="${item[0]}">
                                                    <td><input type="checkbox" class="row-check" name="selectedVariants" value="${item[0]}"></td>
                                                    <td><code>${item[7]}</code></td>
                                                    <td><strong>${item[2]}</strong></td>
                                                    <td>${item[4]} / ${item[6]}</td>
                                                    <td class="text-end"><strong>${physical}</strong></td>
                                                    <td class="text-end">${reserved}</td>
                                                    <td class="text-end">
                                                        <span class="stock-badge ${available <= 10 ? 'stock-low' : 'stock-high'}">${available}</span>
                                                    </td>
                                                    <td>
                                                        <div class="import-form">
                                                            <input type="hidden" name="variantId" value="${item[0]}" disabled>
                                                            <input type="number" name="quantity" min="1" placeholder="Qty" class="qty-input" disabled>
                                                            <input type="number" name="importPrice" min="0" step="1000" placeholder="Price" class="price-input" disabled>
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
                        <div class="pagination-wrapper" style="display: flex; justify-content: center; align-items: center; margin-top: 16px; padding: 0 4px;">
                            <div class="pagination" style="display: flex; gap: 4px;">
                                <c:if test="${invPage > 1}">
                                    <a href="?tab=import&invPage=${invPage - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}&importProductFilter=${importProductFilter}&importImporterFilter=${importImporterFilter}&importDateFrom=${importDateFrom}&importDateTo=${importDateTo}&importSearch=${fn:escapeXml(importSearch)}&importPage=${importPage}"
                                       class="page-btn">&laquo; Prev</a>
                                </c:if>
                                <c:forEach begin="1" end="${invTotalPages > 5 ? 5 : invTotalPages}" var="i">
                                    <c:set var="invStart" value="${invTotalPages > 5 ? (invPage > 3 ? invPage - 2 : 1) : 1}"/>
                                    <a href="?tab=import&invPage=${invStart + i - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}&importProductFilter=${importProductFilter}&importImporterFilter=${importImporterFilter}&importDateFrom=${importDateFrom}&importDateTo=${importDateTo}&importSearch=${fn:escapeXml(importSearch)}&importPage=${importPage}"
                                       class="page-btn ${(invStart + i - 1) == invPage ? 'active' : ''}">${invStart + i - 1}</a>
                                </c:forEach>
                                <c:if test="${invPage < invTotalPages}">
                                    <a href="?tab=import&invPage=${invPage + 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}&importProductFilter=${importProductFilter}&importImporterFilter=${importImporterFilter}&importDateFrom=${importDateFrom}&importDateTo=${importDateTo}&importSearch=${fn:escapeXml(importSearch)}&importPage=${importPage}"
                                       class="page-btn">Next &raquo;</a>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
                </div>
                </form>

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

        // Pre-fill import price from current stock qty / heuristic - leave blank, staff enters
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
            submitBtn.textContent = 'Adding...';
            submitBtn.disabled = true;
            return true;
        };

        refreshCount();
    })();
    </script>
</body>
</html>
