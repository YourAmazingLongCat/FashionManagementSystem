<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Warehouse Inventory - Management</title>
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

        /* Stat Cards */
        .stat-card {
            background: #fff; border-radius: 12px; padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: 1px solid #e2e8f0;
            border-left: 4px solid #1abc9c; transition: 0.2s;
        }
        .stat-card:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,0.08); }
        .stat-card.teal { border-left-color: #1abc9c; }
        .stat-card.blue { border-left-color: #3498db; }
        .stat-card.green { border-left-color: #2ecc71; }
        .stat-card.amber { border-left-color: #f39c12; }
        .stat-card .stat-label { color: #64748b; text-transform: uppercase; font-size: 0.78rem; font-weight: 700; letter-spacing: 0.05em; margin-bottom: 6px; }
        .stat-card .stat-number { font-size: 1.85rem; font-weight: 700; color: #2c3e50; line-height: 1; }

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
        .search-toolbar-modern {
            display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
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
        .btn-search-modern {
            padding: 8px 18px; border-radius: 8px; border: none;
            background: #1abc9c; color: #fff; font-weight: 600; font-size: 0.9rem;
            display: inline-flex; align-items: center; gap: 8px; cursor: pointer; transition: 0.2s;
        }
        .btn-search-modern:hover { background: #16a085; transform: translateY(-1px); color: #fff; }
        .btn-reset-modern {
            padding: 8px 14px; border-radius: 8px; border: 1px solid #cbd5e1;
            background: #fff; color: #64748b; font-weight: 600; font-size: 0.9rem;
            text-decoration: none; transition: 0.2s;
        }
        .btn-reset-modern:hover { background: #f8fafc; color: #2c3e50; }

        /* Table */
        .modern-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
        .modern-table th {
            background: #f1f3f5; color: #2c3e50; font-size: 0.8rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.05em; padding: 12px 14px;
            border: 1px solid #d9dee5; text-align: center; vertical-align: middle;
        }
        .modern-table td {
            padding: 12px 14px; border: 1px solid #d9dee5; vertical-align: middle; text-align: center;
        }
        .modern-table tbody tr:hover { background: #fafbfc; }

        .product-thumb-img { width: 48px; height: 48px; border-radius: 8px; object-fit: contain; border: 1px solid #d9dee5; background: #f8fafc; padding: 2px; }
        .no-thumb { width: 48px; height: 48px; border-radius: 8px; background: #f8fafc; border: 1px solid #d9dee5; display: inline-flex; align-items: center; justify-content: center; color: #94a3b8; font-size: 0.75rem; }

        .sku-tag {
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 0.82rem; font-weight: 700; background: #f1f5f9; color: #2c3e50;
            padding: 4px 8px; border-radius: 6px; border: 1px solid #e2e8f0; display: inline-block;
        }

        .badge-stock {
            display: inline-flex; align-items: center; justify-content: center;
            padding: 4px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 700;
        }
        .badge-in-stock { background: #dcfce7; color: #166534; }
        .badge-medium { background: #fef3c7; color: #92400e; }
        .badge-low { background: #fed7aa; color: #9a3412; }
        .badge-out-of-stock { background: #fee2e2; color: #991b1b; }

        .reserved-pill {
            background: #e0f2fe; color: #0369a1; padding: 2px 8px; border-radius: 6px; font-weight: 700; font-size: 0.82rem;
        }

        /* Pagination */
        .pagination-modern {
            display: flex; justify-content: center; align-items: center; gap: 6px; padding: 16px 20px;
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

            <!-- Subtabs Navigation -->
            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                    <i class="fas fa-boxes"></i> Inventory
                </a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                    <i class="fas fa-edit"></i> Update Quantity
                </a>
            </div>

            <!-- Page Title Header -->
            <div class="page-title-row">
                <div>
                    <h1>Warehouse Inventory</h1>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-danger' : 'alert-success'} mb-4" style="border-radius: 8px; font-weight: 600;">
                    ${message}
                </div>
            </c:if>


            <!-- Table Card -->
            <div class="surface-card">
                <div class="surface-header">
                    <h2 class="surface-header-title">Product Stock List</h2>

                    <!-- Filter & Search Toolbar -->
                    <form class="search-toolbar-modern" method="get" action="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <div class="input-group-search">
                            <i class="fas fa-search"></i>
                            <input class="search-input-modern" type="text" name="keyword" value="${currentKeyword}" placeholder="Search product or SKU..." />
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
                            <i class="fas fa-filter"></i> Apply
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
                                <th style="width: 70px;">Image</th>
                                <th>SKU</th>
                                <th style="text-align: left;">Product Name</th>
                                <th>Category</th>
                                <th>Price</th>
                                <th>Size & Color</th>
                                <th class="text-end">Physical Stock</th>
                                <th class="text-end">Reserved</th>
                                <th class="text-end">Available</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty inventory}">
                                    <tr>
                                        <td colspan="10" class="text-center py-5 text-muted">
                                            <i class="fas fa-box-open fa-2x mb-3 d-block text-muted opacity-50"></i>
                                            <strong>No inventory records found</strong>
                                            <p class="small mb-0">Try refining your search terms or filters.</p>
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
                                                        <div class="no-thumb"><i class="fas fa-image"></i></div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><span class="sku-tag">${item[7]}</span></td>
                                            <td style="text-align: left;"><strong style="color: #2c3e50;">${item[2]}</strong></td>
                                            <td><span style="color: #64748b; font-size: 0.88rem;">${item[13]}</span></td>
                                            <td class="font-monospace fw-bold" style="color: #2c3e50; font-size: 0.9rem;">
                                                <fmt:formatNumber value="${empty item[10] ? item[12] : item[10]}" type="number" groupingUsed="true" /> đ
                                            </td>
                                            <td>
                                                <span style="color: #64748b; font-weight: 500;">Size ${item[4]} &bull; ${item[6]}</span>
                                            </td>
                                            <td class="text-end font-monospace fw-bold" style="font-size: 1rem;">${physical}</td>
                                            <td class="text-end font-monospace">
                                                <c:choose>
                                                    <c:when test="${reserved > 0}">
                                                        <span class="reserved-pill">${reserved}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="fw-bold" style="color: #2c3e50;">0</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end font-monospace fw-bold" style="font-size: 1.05rem; color: ${available == 0 ? '#dc2626' : '#2c3e50'};">
                                                ${available}
                                            </td>
                                            <td>
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
                                <i class="fas fa-chevron-left"></i>
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
                                <i class="fas fa-chevron-right"></i>
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
