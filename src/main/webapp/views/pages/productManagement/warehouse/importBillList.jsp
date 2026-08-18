<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import Bills - Warehouse</title>
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
        .table-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
        .table-header h3 { margin: 0; font-size: 1.05rem; }
        .table-wrapper { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 14px 18px; text-align: left; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
        th { background: #f8fafc; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #64748b; font-weight: 700; white-space: nowrap; }
        tr:hover { background: #fafbff; }
        .bill-id { font-family: monospace; color: #475569; font-size: 0.85rem; }
        .qty-pill { display: inline-flex; align-items: center; justify-content: center; min-width: 60px; padding: 5px 12px; border-radius: 999px; font-weight: 700; font-size: 0.85rem; background: rgba(26, 188, 156, 0.12); color: #0f766e; }
        .price-cell { color: #0f172a; font-weight: 700; }
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
        .filter-bar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; padding: 16px 20px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
        .filter-bar .filter-group { display: flex; align-items: center; gap: 6px; }
        .filter-bar .filter-group label { font-size: 0.8rem; font-weight: 600; color: #64748b; white-space: nowrap; }
        .filter-bar select, .filter-bar input[type="text"] {
            padding: 8px 12px; border: 1px solid #dbe3f0; border-radius: 8px;
            font-size: 0.85rem; outline: none; background: #ffffff; min-width: 140px;
        }
        .filter-bar input[type="date"] {
            padding: 8px 12px; border: 1px solid #dbe3f0; border-radius: 8px;
            font-size: 0.85rem; outline: none; background: #ffffff;
        }
        .filter-bar select:focus, .filter-bar input:focus { border-color: #1abc9c; box-shadow: 0 0 0 2px rgba(26,188,156,0.15); }
        .btn-filter { padding: 8px 16px; background: linear-gradient(135deg, #1abc9c, #16a085); color: #fff; border: none; border-radius: 8px; font-size: 0.85rem; font-weight: 600; cursor: pointer; }
        .btn-filter:hover { transform: translateY(-1px); box-shadow: 0 6px 14px rgba(26, 188, 156, 0.3); }
        .btn-clear { padding: 8px 14px; background: #fff; color: #64748b; border: 1px solid #dbe3f0; border-radius: 8px; font-size: 0.85rem; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; }
        .btn-clear:hover { background: #f1f5f9; }
        .btn-view { padding: 7px 14px; background: #fff; color: #1abc9c; border: 1px solid #1abc9c; border-radius: 8px; font-size: 0.82rem; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; transition: 0.2s; }
        .btn-view:hover { background: #1abc9c; color: #fff; }
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
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Orders</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/payments">Payments</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">Products</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Warehouse</a>
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
                <h4 class="mb-0">Import Bills</h4>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-error' : 'alert-success'}">${message}</div>
            </c:if>

            <!-- Filter Bar -->
            <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import-bills" class="filter-bar">
                <div class="filter-group">
                    <label>Search:</label>
                    <input type="text" name="search" value="${billSearch}" placeholder="Search by importer name..." />
                </div>
                <div class="filter-group">
                    <label>Importer:</label>
                    <select name="importerFilter">
                        <option value="">All Importers</option>
                        <c:forEach var="imp" items="${importers}">
                            <option value="${imp[0]}" ${importerFilter eq imp[0] ? 'selected' : ''}>${imp[1]}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-group">
                    <label>From:</label>
                    <input type="date" name="dateFrom" value="${dateFrom}" />
                </div>
                <div class="filter-group">
                    <label>To:</label>
                    <input type="date" name="dateTo" value="${dateTo}" />
                </div>
                <button type="submit" class="btn-filter">Filter</button>
                <a href="${pageContext.request.contextPath}/staff/warehouse/import-bills" class="btn-clear">Clear</a>
            </form>

            <!-- Import Bills Table -->
            <div class="table-panel">
                <div class="table-header">
                    <h3>Import Bills List</h3>
                </div>
                <div class="table-wrapper">
                    <table>
                        <thead>
                            <tr>
                                <th>Bill ID</th>
                                <th>Imported By</th>
                                <th>Date</th>
                                <th class="text-end">Items</th>
                                <th class="text-end">Total Quantity</th>
                                <th class="text-end">Total Price</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty importBills}">
                                    <tr>
                                        <td colspan="7">
                                            <div class="empty-state">
                                                <h4>No import bills found</h4>
                                                <p>Try changing your filter or perform a Stock-In action first</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="bill" items="${importBills}">
                                        <tr>
                                            <td><span class="bill-id">BILL-${bill[1].toString().replace(' ', 'T').replace(':', '').replace('.', '').substring(0, 12)}</span></td>
                                            <td><strong>${bill[3]}</strong></td>
                                            <td><fmt:formatDate value="${bill[1]}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                            <td class="text-end">${bill[4]}</td>
                                            <td class="text-end"><span class="qty-pill">${bill[5]}</span></td>
                                            <td class="text-end price-cell"><fmt:formatNumber value="${bill[6]}" pattern="#,##0"/> VND</td>
                                            <td class="text-center">
                                                <c:url value="/staff/warehouse/import-bills/view" var="billDetailUrl">
                                                    <c:param name="billKey" value="${bill[0]}" />
                                                </c:url>
                                                <a class="btn-view" href="${billDetailUrl}">View Detail</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <c:if test="${billTotalPages > 1}">
                    <div class="pagination-bar">
                        <span class="pagination-summary">Showing ${importBills.size()} of ${billTotalRecords} bills</span>
                        <div class="pagination-controls">
                            <c:if test="${billPage > 1}">
                                <a class="page-link-inv" href="?billPage=${billPage - 1}&search=${fn:escapeXml(billSearch)}&importerFilter=${importerFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">&#8249; Prev</a>
                            </c:if>
                            <c:forEach begin="1" end="${billTotalPages > 5 ? 5 : billTotalPages}" var="i">
                                <c:set var="billStart" value="${billTotalPages > 5 ? (billPage > 3 ? (billPage + 2 > billTotalPages ? billTotalPages - 4 : billPage - 2) : 1) : 1}"/>
                                <a class="page-link-inv ${(billStart + i - 1) == billPage ? 'active' : ''}" href="?billPage=${billStart + i - 1}&search=${fn:escapeXml(billSearch)}&importerFilter=${importerFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">${billStart + i - 1}</a>
                            </c:forEach>
                            <c:if test="${billPage < billTotalPages}">
                                <a class="page-link-inv" href="?billPage=${billPage + 1}&search=${fn:escapeXml(billSearch)}&importerFilter=${importerFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">Next &#8250;</a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>