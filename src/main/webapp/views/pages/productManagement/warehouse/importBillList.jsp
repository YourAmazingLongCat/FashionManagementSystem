<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import Bills - Management</title>
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
            display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
            padding: 16px 20px; background: #fafbfc; border-bottom: 1px solid #e2e8f0;
        }
        .input-group-search { position: relative; min-width: 220px; }
        .input-group-search i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; pointer-events: none; }
        .search-input-modern {
            width: 100%; min-height: 38px; padding: 8px 12px 8px 36px;
            border-radius: 8px; border: 1px solid #cbd5e1; background: #fff;
            font: inherit; font-size: 0.9rem; color: #2c3e50; outline: none; transition: 0.2s;
        }
        .search-input-modern:focus { border-color: #1abc9c; box-shadow: 0 0 0 3px rgba(26, 188, 156, 0.15); }
        .select-filter-modern, .date-filter-modern {
            min-height: 38px; padding: 8px 12px; border-radius: 8px; border: 1px solid #cbd5e1;
            background: #fff; font: inherit; font-size: 0.9rem; color: #2c3e50; outline: none;
        }
        .select-filter-modern:focus, .date-filter-modern:focus { border-color: #1abc9c; box-shadow: 0 0 0 3px rgba(26, 188, 156, 0.15); }

        /* Buttons */
        .btn-action-primary {
            padding: 8px 18px; border-radius: 8px; border: none;
            background: #1abc9c; color: #fff; font-weight: 600; font-size: 0.9rem;
            display: inline-flex; align-items: center; gap: 8px; cursor: pointer; transition: 0.2s;
        }
        .btn-action-primary:hover { background: #16a085; transform: translateY(-1px); color: #fff; }
        .btn-action-secondary {
            padding: 8px 14px; border-radius: 8px; border: 1px solid #cbd5e1;
            background: #fff; color: #64748b; font-weight: 600; font-size: 0.9rem;
            text-decoration: none; transition: 0.2s;
        }
        .btn-action-secondary:hover { background: #f8fafc; color: #2c3e50; }

        .btn-view-modern {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 6px 12px; border-radius: 6px; font-weight: 600; font-size: 0.82rem;
            background: #e0f2fe; color: #0369a1; text-decoration: none; transition: 0.2s;
        }
        .btn-view-modern:hover { background: #bae6fd; color: #0284c7; }

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

        .bill-id-tag {
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 0.82rem; font-weight: 700; background: #f1f5f9; color: #2c3e50;
            padding: 4px 8px; border-radius: 6px; border: 1px solid #e2e8f0; display: inline-block;
        }

        .qty-pill-modern {
            background: #f8fafc; color: #2c3e50; border: 1px solid #e2e8f0;
            padding: 2px 8px; border-radius: 6px; font-weight: 700; font-size: 0.85rem;
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

            <!-- Subtabs -->
            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                    <i class="fas fa-boxes"></i> Inventory
                </a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                    <i class="fas fa-dolly"></i> Stock In
                </a>
                <a class="${activeTab eq 'import-bills' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">
                    <i class="fas fa-file-invoice-dollar"></i> Import Bills
                </a>
            </div>

            <!-- Page Title -->
            <div class="page-title-row">
                <div>
                    <h1>Import Bills History</h1>
                    <p>Track historical inbound warehouse batches, total costs, and receipts.</p>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert ${messageType eq 'error' ? 'alert-danger' : 'alert-success'} mb-4" style="border-radius: 8px; font-weight: 600;">
                    ${message}
                </div>
            </c:if>

            <div class="surface-card">
                <!-- Filter Bar -->
                <form method="get" action="${pageContext.request.contextPath}/staff/warehouse/import-bills" class="filter-bar-modern">
                    <div class="input-group-search">
                        <i class="fas fa-search"></i>
                        <input type="text" class="search-input-modern" name="search" value="${billSearch}" placeholder="Search by importer name..." />
                    </div>
                    <select class="select-filter-modern" name="importerFilter">
                        <option value="">All Importers</option>
                        <c:forEach var="imp" items="${importers}">
                            <option value="${imp[0]}" ${importerFilter eq imp[0] ? 'selected' : ''}>${imp[1]}</option>
                        </c:forEach>
                    </select>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 0.82rem; font-weight: 600; color: #64748b;">From:</span>
                        <input type="date" class="date-filter-modern" name="dateFrom" value="${dateFrom}" />
                    </div>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 0.82rem; font-weight: 600; color: #64748b;">To:</span>
                        <input type="date" class="date-filter-modern" name="dateTo" value="${dateTo}" />
                    </div>
                    <button type="submit" class="btn-action-primary">
                        <i class="fas fa-filter"></i> Filter
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
                                <th>Distinct Items</th>
                                <th>Total Quantity</th>
                                <th>Total Amount</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty importBills}">
                                    <tr>
                                        <td colspan="7" style="padding: 48px 20px; text-align: center; color: #64748b;">
                                            No import bills found.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="bill" items="${importBills}">
                                        <tr>
                                            <td><span class="bill-id-tag">BILL-${bill[1].toString().replace(' ', 'T').replace(':', '').replace('.', '').substring(0, 12)}</span></td>
                                            <td><strong style="color: #2c3e50;">${bill[3]}</strong></td>
                                            <td><fmt:formatDate value="${bill[1]}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                            <td class="text-end font-monospace fw-bold">${bill[4]}</td>
                                            <td class="text-end"><span class="qty-pill-modern">${bill[5]}</span></td>
                                            <td class="text-end font-monospace fw-bold" style="color: #2c3e50; font-size: 0.96rem;">
                                                <fmt:formatNumber value="${bill[6]}" pattern="#,##0"/> đ
                                            </td>
                                            <td class="text-center">
                                                <c:url value="/staff/warehouse/import-bills/view" var="billDetailUrl">
                                                    <c:param name="billKey" value="${bill[0]}" />
                                                </c:url>
                                                <a class="btn-view-modern" href="${billDetailUrl}">
                                                    <i class="fas fa-eye"></i> View Detail
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
                                <i class="fas fa-chevron-left"></i>
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