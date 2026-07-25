<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Stock In - Warehouse</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260617-warehouse">
        <style>
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .warehouse-shell { width: min(1440px, calc(100% - 40px)); margin: 28px auto; display: grid; grid-template-columns: 280px 1fr; gap: 24px; }
            .sidebar-panel, .content-panel { background: #ffffff; border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08); contain: content; }
            .sidebar-panel { padding: 28px; display: flex; flex-direction: column; gap: 20px; background: linear-gradient(180deg, #0f172a 0%, #1e1b4b 100%); color: #ffffff; }
            .brand-label, .sidebar-label { margin: 0 0 8px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.72rem; font-weight: 700; color: #94a3b8; }
            .sidebar-panel h1 { margin: 0; font-size: 1.6rem; line-height: 1.2; }
            .sidebar-text { margin: 10px 0 0; color: rgba(255, 255, 255, 0.7); font-size: 0.88rem; line-height: 1.6; }
            .sidebar-tabs { display: flex; flex-direction: column; gap: 8px; }
            .sidebar-tab { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 14px; color: rgba(255, 255, 255, 0.85); text-decoration: none; font-weight: 600; font-size: 0.92rem; transition: all 0.2s ease; border: 1px solid transparent; }
            .sidebar-tab:hover { background: rgba(255, 255, 255, 0.1); }
            .sidebar-tab.active { background: #ffffff; color: #1e1b4b; box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); }
            .sidebar-tab .icon { font-size: 1.3rem; }
            .back-link { display: inline-flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 14px; background: rgba(255, 255, 255, 0.1); color: #ffffff; text-decoration: none; font-weight: 600; font-size: 0.88rem; margin-top: auto; }
            .back-link:hover { background: rgba(255, 255, 255, 0.15); }
            .content-panel { padding: 28px; }
            .page-header { margin-bottom: 24px; }
            .page-header h2 { margin: 0 0 6px; font-size: 1.8rem; }
            .page-header p { margin: 0; color: #64748b; font-size: 0.95rem; }
            .alert { padding: 14px 16px; border-radius: 12px; font-weight: 600; margin-bottom: 20px; }
            .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
            .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
            .table-panel { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 20px; overflow: hidden; margin-bottom: 24px; }
            .table-header { padding: 20px 24px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
            .table-header h3 { margin: 0; font-size: 1.1rem; }
            .table-wrapper { overflow-x: auto; }
            table { width: 100%; border-collapse: collapse; }
            th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f1f5f9; }
            th { background: #f8fafc; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #64748b; font-weight: 700; white-space: nowrap; }
            tr:hover { background: #fafbff; }
            .stock-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 60px; padding: 4px 10px; border-radius: 999px; font-weight: 700; font-size: 0.8rem; }
            .stock-high { background: rgba(22, 163, 74, 0.12); color: #16a34a; }
            .stock-low { background: rgba(220, 38, 38, 0.12); color: #dc2626; }
            .btn { padding: 8px 16px; border-radius: 8px; border: none; font-weight: 700; font-size: 0.85rem; cursor: pointer; }
            .btn-primary { background: #16a34a; color: #ffffff; }
            .btn-primary:hover { background: #15803d; }
            .reset-btn { padding: 9px 16px; background: #fff; color: #64748b; border: 1px solid #dbe3f0; border-radius: 10px; font-size: 0.88rem; font-weight: 600; text-decoration: none; cursor: pointer; display: inline-flex; align-items: center; }
            .reset-btn:hover { background: #f1f5f9; }
            .import-form { display: flex; gap: 8px; align-items: center; }
            .import-form input[type="number"] { width: 80px; padding: 8px; border-radius: 8px; border: 1px solid #dbe3f0; }
            .import-form input[type="number"].price-input { width: 100px; }
            .history-section { margin-top: 24px; }
            .empty-state { padding: 32px; text-align: center; color: #64748b; }
            .filter-bar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; padding: 16px 20px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
            .filter-bar .filter-group { display: flex; align-items: center; gap: 6px; }
            .filter-bar .filter-group label { font-size: 0.8rem; font-weight: 600; color: #64748b; white-space: nowrap; }
            .filter-bar select, .filter-bar input[type="text"] {
                padding: 6px 10px; border: 1px solid #dbe3f0; border-radius: 8px;
                font-size: 0.85rem; outline: none; background: #ffffff; min-width: 130px;
            }
            .filter-bar select:focus, .filter-bar input[type="text"]:focus { border-color: #16a34a; box-shadow: 0 0 0 2px rgba(22,163,74,0.1); }
            .filter-bar .btn-filter { padding: 6px 14px; background: #16a34a; color: #fff; border: none; border-radius: 8px; font-size: 0.82rem; font-weight: 600; cursor: pointer; }
            .filter-bar .btn-filter:hover { background: #15803d; }
            .filter-bar .btn-clear { padding: 6px 12px; background: #fff; color: #64748b; border: 1px solid #dbe3f0; border-radius: 8px; font-size: 0.82rem; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; }
            .filter-bar .btn-clear:hover { background: #f1f5f9; }
            .page-btn { display: inline-flex; align-items: center; justify-content: center; min-width: 36px; height: 36px; padding: 0 10px; border-radius: 10px; border: 1px solid #dbe3f0; background: #fff; color: #334155; font-size: 0.85rem; font-weight: 600; text-decoration: none; transition: all 0.2s ease; }
            .page-btn:hover { background: #f1f5f9; border-color: #16a34a; color: #16a34a; }
            .page-btn.active { background: #16a34a; border-color: #16a34a; color: #fff; }
            @media (max-width: 1024px) { .warehouse-shell { grid-template-columns: 1fr; } }
            @media (max-width: 768px) { .warehouse-shell { width: min(100% - 20px, 100%); margin: 16px auto; } }
        </style>
    </head>
    <body>
        <div class="warehouse-shell">
            <aside class="sidebar-panel">
                <div>
                    <p class="brand-label">Fashion Shop</p>
                    <h1>Warehouse</h1>
                    <p class="sidebar-text">Inventory management, stock in/out</p>
                </div>
                <div class="sidebar-tabs">
                    <a class="sidebar-tab ${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <span class="icon">&#128203;</span>
                        <span>Inventory</span>
                    </a>
                    <a class="sidebar-tab ${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                        <span class="icon">&#10133;</span>
                        <span>Stock In</span>
                    </a>
                </div>
                <a class="back-link" href="${pageContext.request.contextPath}/staff/products">
                    &#8592; Back
                </a>
            </aside>

            <main class="content-panel">
                <div class="page-header">
                    <h2>Stock In</h2>
                    <p>Add stock quantity for each variant with import price tracking</p>
                </div>

                <c:if test="${not empty message}">
                    <div class="alert ${messageType eq 'error' ? 'alert-error' : 'alert-success'}">${message}</div>
                </c:if>

                <!-- Filter & Search Bar -->
                <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import" class="filter-bar" id="inventoryFilterForm">
                    <div class="filter-group">
                        <label><i class="fas fa-search"></i></label>
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
                    <button type="submit" class="btn-filter"><i class="fas fa-filter"></i> Filter</button>
                    <a href="${pageContext.request.contextPath}/staff/warehouse/import" class="btn-clear"><i class="fas fa-times"></i> Clear</a>
                </form>

                <div class="table-panel">
                    <div class="table-header">
                        <h3>Variant List</h3>
                    </div>
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
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
                                            <td colspan="7">
                                                <div class="empty-state">No product variants found. Check that Products and ProductVariants tables have data.</div>
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="item" items="${inventory}">
                                            <c:set var="physical" value="${item[8]}" />
                                            <c:set var="reserved" value="${item[9]}" />
                                            <c:set var="available" value="${physical - reserved}" />
                                            <tr>
                                                <td><code>${item[7]}</code></td>
                                                <td><strong>${item[2]}</strong></td>
                                                <td>${item[4]} / ${item[6]}</td>
                                                <td class="text-end"><strong>${physical}</strong></td>
                                                <td class="text-end">${reserved}</td>
                                                <td class="text-end">
                                                    <span class="stock-badge ${available <= 10 ? 'stock-low' : 'stock-high'}">${available}</span>
                                                </td>
                                                <td>
                                                    <form class="import-form" method="post" action="${pageContext.request.contextPath}/staff/warehouse/import">
                                                        <input type="hidden" name="action" value="import">
                                                        <input type="hidden" name="variantId" value="${item[0]}">
                                                        <input type="number" name="quantity" min="1" placeholder="Qty" required>
                                                        <input type="number" name="importPrice" min="0" step="1000" placeholder="Price" required>
                                                        <button type="submit" class="btn btn-primary">+ Add</button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${invTotalPages > 1}">
                        <div class="pagination-wrapper" style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px; padding: 0 4px;">
                            <span style="font-size: 0.85rem; color: #64748b;">
                                Showing ${inventory.size()} of ${invTotalRecords} variants
                            </span>
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

                <!-- Recent Imports History -->
                <div class="table-panel history-section">
                    <div class="table-header">
                        <h3>Recent Imports</h3>
                    </div>
                    <div class="filter-bar" style="margin-bottom: 16px;">
                        <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                            <input type="hidden" name="tab" value="import">
                            <input type="hidden" name="keyword" value="${currentKeyword}">
                            <input type="hidden" name="productFilter" value="${currentProductFilter}">
                            <input type="hidden" name="colorFilter" value="${currentColorFilter}">

                            <input type="text" name="importSearch" value="${importSearch}" placeholder="Search product, importer..." style="padding: 9px 14px; border-radius: 10px; border: 1px solid #dbe3f0; font-size: 0.88rem; min-width: 200px; outline: none;"/>

                            <select name="importProductFilter" style="padding: 9px 14px; border-radius: 10px; border: 1px solid #dbe3f0; font-size: 0.88rem; min-width: 160px;">
                                <option value="">All Products</option>
                                <c:forEach var="p" items="${products}">
                                    <option value="${p.productId}" ${importProductFilter eq p.productId ? 'selected' : ''}>${p.name}</option>
                                </c:forEach>
                            </select>

                            <select name="importImporterFilter" style="padding: 9px 14px; border-radius: 10px; border: 1px solid #dbe3f0; font-size: 0.88rem; min-width: 150px;">
                                <option value="">All Importers</option>
                                <c:forEach var="imp" items="${importers}">
                                    <option value="${imp[0]}" ${importImporterFilter eq imp[0] ? 'selected' : ''}>${imp[1]}</option>
                                </c:forEach>
                            </select>

                            <input type="date" name="importDateFrom" value="${importDateFrom}" style="padding: 9px 14px; border-radius: 10px; border: 1px solid #dbe3f0; font-size: 0.88rem;"/>
                            <span style="color: #94a3b8;">—</span>
                            <input type="date" name="importDateTo" value="${importDateTo}" style="padding: 9px 14px; border-radius: 10px; border: 1px solid #dbe3f0; font-size: 0.88rem;"/>

                            <button type="submit" class="btn btn-primary" style="padding: 9px 18px; font-size: 0.88rem;">Filter</button>
                            <a href="?tab=import&importProductFilter=&importImporterFilter=&importDateFrom=&importDateTo=&importSearch=&keyword=${currentKeyword}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}" class="reset-btn" style="padding: 9px 16px; font-size: 0.88rem; text-decoration: none;">Reset</a>
                        </form>
                    </div>
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Import ID</th>
                                    <th>Product</th>
                                    <th>Size / Color</th>
                                    <th class="text-end">Quantity</th>
                                    <th class="text-end">Import Price</th>
                                    <th>Imported By</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty recentImports}">
                                        <tr>
                                            <td colspan="7">
                                                <div class="empty-state">No import records found</div>
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="imp" items="${recentImports}">
                                            <tr>
                                                <td><code>${imp[0]}</code></td>
                                                <td><strong>${imp[7]}</strong></td>
                                                <td>${imp[8]} / ${imp[9]}</td>
                                                <td class="text-end"><strong>+${imp[2]}</strong></td>
                                                <td class="text-end"><fmt:formatNumber value="${imp[3]}" pattern="#,##0"/> VND</td>
                                                <td>${imp[6]}</td>
                                                <td><fmt:formatDate value="${imp[5]}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>

                        <c:if test="${importTotalPages > 1}">
                            <div class="pagination-wrapper" style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px; padding: 0 4px;">
                                <span style="font-size: 0.85rem; color: #64748b;">
                                    Showing ${recentImports.size()} of ${importTotalRecords} records
                                </span>
                                <div class="pagination" style="display: flex; gap: 4px;">
                                    <c:if test="${importPage > 1}">
                                        <a href="?tab=import&invPage=${invPage}&importPage=${importPage - 1}&importProductFilter=${importProductFilter}&importImporterFilter=${importImporterFilter}&importDateFrom=${importDateFrom}&importDateTo=${importDateTo}&importSearch=${importSearch}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}"
                                           class="page-btn">&laquo; Prev</a>
                                    </c:if>
                                    <c:forEach begin="1" end="${importTotalPages > 5 ? 5 : importTotalPages}" var="i">
                                        <c:set var="startPage" value="${importTotalPages > 5 ? (importPage > 3 ? importPage - 2 : 1) : 1}"/>
                                        <a href="?tab=import&invPage=${invPage}&importPage=${startPage + i - 1}&importProductFilter=${importProductFilter}&importImporterFilter=${importImporterFilter}&importDateFrom=${importDateFrom}&importDateTo=${importDateTo}&importSearch=${importSearch}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}"
                                           class="page-btn ${(startPage + i - 1) == importPage ? 'active' : ''}">${startPage + i - 1}</a>
                                    </c:forEach>
                                    <c:if test="${importPage < importTotalPages}">
                                        <a href="?tab=import&invPage=${invPage}&importPage=${importPage + 1}&importProductFilter=${importProductFilter}&importImporterFilter=${importImporterFilter}&importDateFrom=${importDateFrom}&importDateTo=${importDateTo}&importSearch=${importSearch}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}"
                                           class="page-btn">Next &raquo;</a>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </main>
        </div>
    </body>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('.import-form').forEach(function(form) {
            form.addEventListener('submit', function(e) {
                e.preventDefault();
                var btn = form.querySelector('button[type="submit"]');
                var originalText = btn.textContent;
                btn.textContent = 'Adding...';
                btn.disabled = true;

                var formData = new FormData(form);
                fetch(form.action, { method: 'POST', body: formData })
                    .then(function(resp) {
                        if (resp.redirected) {
                            window.location.href = resp.url;
                        }
                    })
                    .then(function() {
                        btn.textContent = originalText;
                        btn.disabled = false;
                        form.querySelector('input[name="quantity"]').value = '';
                        form.querySelector('input[name="importPrice"]').value = '';
                    })
                    .catch(function() {
                        btn.textContent = originalText;
                        btn.disabled = false;
                    });
            });
        });
    });
    </script>
</html>
