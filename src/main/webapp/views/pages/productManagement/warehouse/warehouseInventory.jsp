<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inventory - Warehouse</title>
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
        .stat-card {
            background: #fff; border-radius: 12px; padding: 20px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-left: 4px solid #1abc9c;
        }
        .stat-card .stat-number { font-size: 2rem; font-weight: 700; }
        .stat-card .stat-label { color: #6c757d; text-transform: uppercase; font-size: 0.9rem; }
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .card-header { background: #f8f9fa; font-weight: 600; }
        .table th { background: #f1f3f5; border-top: none; }
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
        .stat-block {
            background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 22px;
        }
        .stat-block .label { font-size: 0.82rem; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }
        .stat-block .value { font-size: 2rem; font-weight: 800; color: #0f172a; }
        .stat-block.warning { background: #fffbeb; border-color: #fde68a; }
        .stat-block.warning .value { color: #d97706; }
        .alert { padding: 14px 16px; border-radius: 12px; font-weight: 600; margin-bottom: 18px; }
        .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
        .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
        .table-panel { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
        .table-header { padding: 18px 22px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; flex-wrap: wrap; gap: 16px; }
        .table-header h3 { margin: 0; font-size: 1.05rem; }
        .search-toolbar { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
        .search-input { padding: 10px 14px; border-radius: 10px; border: 1px solid #dbe3f0; background: #fff; font-family: inherit; font-size: 0.9rem; min-width: 220px; outline: none; }
        .search-input:focus { border-color: #1abc9c; box-shadow: 0 0 0 3px rgba(26, 188, 156, 0.15); }
        .filter-select { padding: 10px 30px 10px 14px; border-radius: 10px; border: 1px solid #dbe3f0; background: #fff; font-family: inherit; font-size: 0.9rem; cursor: pointer; outline: none; }
        .filter-select:focus { border-color: #1abc9c; }
        .search-btn { padding: 10px 18px; border-radius: 10px; border: none; background: linear-gradient(135deg, #1abc9c, #16a085); color: #fff; font-weight: 600; font-size: 0.88rem; cursor: pointer; transition: 0.2s; }
        .search-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 14px rgba(26, 188, 156, 0.3); }
        .reset-btn { padding: 10px 18px; border-radius: 10px; border: 1px solid #dbe3f0; background: #fff; color: #334155; font-weight: 600; font-size: 0.88rem; text-decoration: none; }
        .reset-btn:hover { background: #f8fafc; }
        .table-wrapper { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 14px 18px; text-align: left; border-bottom: 1px solid #f1f5f9; }
        th { background: #f8f9fa; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #64748b; font-weight: 700; white-space: nowrap; }
        tr:hover { background: #fafbff; }
        .stock-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 60px; padding: 6px 12px; border-radius: 999px; font-weight: 700; font-size: 0.85rem; }
        .stock-high { background: rgba(22, 163, 74, 0.12); color: #16a34a; }
        .stock-medium { background: rgba(245, 158, 11, 0.12); color: #b45309; }
        .stock-low { background: rgba(220, 38, 38, 0.12); color: #dc2626; }
        .stock-zero { background: rgba(107, 114, 128, 0.12); color: #6b7280; }
        .reserved-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 50px; padding: 4px 10px; border-radius: 999px; font-weight: 600; font-size: 0.8rem; background: rgba(99, 102, 241, 0.12); color: #4f46e5; }
        .empty-state { padding: 40px 20px; text-align: center; color: #64748b; }
        .empty-state h4 { margin: 0 0 6px; font-size: 1.05rem; color: #334155; }
        .empty-state p { margin: 0; }
        .pagination-bar { display: flex; justify-content: space-between; align-items: center; padding: 14px 4px 4px; flex-wrap: wrap; gap: 12px; }
        .pagination-summary { color: #64748b; font-size: 0.88rem; font-weight: 600; }
        .pagination-controls { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
        .page-link-inv { min-width: 36px; height: 36px; padding: 0 12px; border-radius: 8px; border: 1px solid #dbe3f0; background: #ffffff; color: #334155; display: inline-flex; align-items: center; justify-content: center; text-decoration: none; font-weight: 600; font-size: 0.88rem; transition: 0.2s; }
        .page-link-inv:hover { background: #f1f5f9; border-color: #1abc9c; color: #1abc9c; }
        .page-link-inv.active { background: linear-gradient(135deg, #1abc9c, #16a085); color: #ffffff; border-color: transparent; }
        .page-link-inv.disabled { opacity: 0.4; pointer-events: none; }
        @media (max-width: 1024px) { .warehouse-shell { grid-template-columns: 1fr !important; } }
        @media (max-width: 768px) { .sidebar { min-height: auto; height: auto; } .main-content { padding: 15px; } .stat-block .value { font-size: 1.5rem; } }
        </style>
    </head>
    <body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand">Management</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Manage Orders</a>
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
                <h4 class="mb-0">Inventory</h4>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-error' : 'alert-success'}">${message}</div>
            </c:if>

            <div class="row g-3 mb-4">
                <div class="col-md-3 col-6"><div class="stat-block"><div class="label">Total Variants</div><div class="value">${totalItems}</div></div></div>
                <div class="col-md-3 col-6"><div class="stat-block"><div class="label">Total Physical Stock</div><div class="value">${totalStock}</div></div></div>
                <div class="col-md-3 col-6"><div class="stat-block"><div class="label">Total Available</div><div class="value">${totalAvailable}</div></div></div>
                <div class="col-md-3 col-6"><div class="stat-block warning"><div class="label">Low Stock</div><div class="value">${lowStockCount}</div></div></div>
            </div>

            <div class="card">
                <div class="card-header">
                    <span>Stock List</span>
                </div>
                <div class="card-body">
                    <form class="search-toolbar mb-3" method="get" action="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <input class="search-input" type="text" name="keyword" value="${currentKeyword}" placeholder="Search by product name or SKU..." />
                        <select class="filter-select" name="productFilter">
                            <option value="">All Products</option>
                            <c:forEach var="p" items="${products}">
                                <option value="${p.productId}" ${currentProductFilter eq p.productId ? 'selected' : ''}>${p.productName}</option>
                            </c:forEach>
                        </select>
                        <select class="filter-select" name="colorFilter">
                            <option value="">All Colors</option>
                            <c:forEach var="color" items="${allColors}">
                                <option value="${color[0]}" ${currentColorFilter eq color[0] ? 'selected' : ''}>${color[1]}</option>
                            </c:forEach>
                        </select>
                        <button class="search-btn" type="submit">Search</button>
                        <a class="reset-btn" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Reset</a>
                    </form>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>SKU</th>
                                    <th>Product</th>
                                    <th>Size / Color</th>
                                    <th class="text-end">Physical</th>
                                    <th class="text-end">Reserved</th>
                                    <th class="text-end">Available</th>
                                    <th class="text-end">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty inventory}">
                                        <tr>
                                            <td colspan="7">
                                                <div class="empty-state">
                                                    <h4>No data</h4>
                                                    <p>No products in inventory</p>
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
                                                <td><code>${item[7]}</code></td>
                                                <td><strong>${item[2]}</strong></td>
                                                <td>${item[4]} / ${item[6]}</td>
                                                <td class="text-end"><strong>${physical}</strong></td>
                                                <td class="text-end"><span class="reserved-badge">${reserved}</span></td>
                                                <td class="text-end"><strong>${available}</strong></td>
                                                <td class="text-end">
                                                    <c:choose>
                                                        <c:when test="${available == 0}">
                                                            <span class="stock-badge stock-zero">Out of Stock</span>
                                                        </c:when>
                                                        <c:when test="${available <= 5}">
                                                            <span class="stock-badge stock-low">Low</span>
                                                        </c:when>
                                                        <c:when test="${available <= 20}">
                                                            <span class="stock-badge stock-medium">Medium</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="stock-badge stock-high">In Stock</span>
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
                        <div class="pagination-bar" style="justify-content: center;">
                            <div class="pagination-controls">
                                <c:if test="${invPage > 1}">
                                    <a class="page-link-inv" href="?activeTab=inventory&invPage=${invPage - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}">&#8249; Prev</a>
                                </c:if>
                                <c:forEach begin="1" end="${invTotalPages > 5 ? 5 : invTotalPages}" var="i">
                                    <c:set var="invStart" value="${invTotalPages > 5 ? (invPage > 3 ? (invPage + 2 > invTotalPages ? invTotalPages - 4 : invPage - 2) : 1) : 1}"/>
                                    <a class="page-link-inv ${(invStart + i - 1) == invPage ? 'active' : ''}" href="?activeTab=inventory&invPage=${invStart + i - 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}">${invStart + i - 1}</a>
                                </c:forEach>
                                <c:if test="${invPage < invTotalPages}">
                                    <a class="page-link-inv" href="?activeTab=inventory&invPage=${invPage + 1}&keyword=${fn:escapeXml(currentKeyword)}&productFilter=${currentProductFilter}&colorFilter=${currentColorFilter}">Next &#8250;</a>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
